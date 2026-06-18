#!/usr/bin/env python3
"""
Migra los DATOS de tu base MySQL local a la base PostgreSQL (Render).

El ESQUEMA (las tablas) NO se migra: lo crea tu aplicacion Spring Boot al
arrancar gracias a `spring.jpa.hibernate.ddl-auto=update`. Como ambas bases
se crean con las mismas entidades de Hibernate, los nombres de tablas y
columnas son identicos, por eso la copia puede ser generica.

QUE HACE:
  1. Lee todas las tablas de MySQL.
  2. Calcula el orden de insercion respetando las llaves foraneas.
  3. Copia los datos (maneja blobs/bytea, fechas, decimales y NULLs).
  4. Resetea las secuencias de los IDs autogenerados en PostgreSQL.

REQUISITOS (instalar una sola vez):
    pip install pymysql psycopg2-binary

PRE-REQUISITO IMPORTANTE:
    La aplicacion debe haber arrancado al menos una vez contra la Postgres de
    Render para que las tablas ya existan (vacias). Si no, este script fallara
    porque no encontrara las tablas destino.

USO (PowerShell):
    $env:MYSQL_PASSWORD = ""              # vacia si tu XAMPP no tiene clave
    $env:POSTGRES_URL = "postgresql://vehiculos:BzKZ2qsVWD3yqRAY4xqe4C3ww9WoNaAd@dpg-d8p2np0g4nts73fh3ht0-a.oregon-postgres.render.com/vehiculos_db_pxwv"
    python scripts/migrar_mysql_a_postgres.py

El POSTGRES_URL es la "External Database URL" que te da Render en el panel de
la base de datos vehiculos-db (empieza con postgresql://).
"""

import os
import sys
from collections import defaultdict, deque

try:
    import pymysql
    import psycopg2
    from psycopg2 import sql
    from psycopg2.extras import execute_values
except ImportError:
    sys.exit(
        "Faltan librerias. Instalalas con:\n"
        "    pip install pymysql psycopg2-binary"
    )

# --------------------------------------------------------------------------
# Configuracion (puedes editar aqui o usar variables de entorno)
# --------------------------------------------------------------------------
MYSQL_CONFIG = {
    "host": os.getenv("MYSQL_HOST", "127.0.0.1"),
    "port": int(os.getenv("MYSQL_PORT", "3306")),
    "user": os.getenv("MYSQL_USER", "root"),
    "password": os.getenv("MYSQL_PASSWORD", ""),
    "database": os.getenv("MYSQL_DATABASE", "vehiculos_db"),
    "charset": "utf8mb4",
}

# URL completa de la Postgres de Render (External Database URL).
POSTGRES_URL = os.getenv(
    "POSTGRES_URL",
    "postgresql://postgres:postgres@localhost:5432/vehiculos_db",
)

# Tablas que NO se copian (staging/transitorias). Ajusta si lo necesitas.
TABLAS_A_OMITIR = {"trayecto_tmp"}


def obtener_tablas(mysql_cur, db):
    mysql_cur.execute(
        """
        SELECT TABLE_NAME
        FROM information_schema.TABLES
        WHERE TABLE_SCHEMA = %s AND TABLE_TYPE = 'BASE TABLE'
        """,
        (db,),
    )
    return [r[0] for r in mysql_cur.fetchall()]


def obtener_dependencias_fk(mysql_cur, db):
    """Devuelve aristas (tabla -> tabla_referenciada) de las llaves foraneas."""
    mysql_cur.execute(
        """
        SELECT TABLE_NAME, REFERENCED_TABLE_NAME
        FROM information_schema.KEY_COLUMN_USAGE
        WHERE TABLE_SCHEMA = %s AND REFERENCED_TABLE_NAME IS NOT NULL
        """,
        (db,),
    )
    edges = set()
    for tabla, referenciada in mysql_cur.fetchall():
        if tabla != referenciada:  # ignoramos auto-referencias
            edges.add((tabla, referenciada))
    return edges


def ordenar_por_dependencias(tablas, edges):
    """Orden topologico: las tablas referenciadas van primero (Kahn)."""
    tablas = set(tablas)
    # depende_de[t] = conjunto de tablas que deben insertarse antes que t
    depende_de = defaultdict(set)
    dependientes = defaultdict(set)
    for tabla, referenciada in edges:
        if tabla in tablas and referenciada in tablas:
            depende_de[tabla].add(referenciada)
            dependientes[referenciada].add(tabla)

    indegree = {t: len(depende_de[t]) for t in tablas}
    cola = deque(sorted(t for t in tablas if indegree[t] == 0))
    orden = []
    while cola:
        t = cola.popleft()
        orden.append(t)
        for dep in sorted(dependientes[t]):
            indegree[dep] -= 1
            if indegree[dep] == 0:
                cola.append(dep)

    if len(orden) != len(tablas):
        # Hay un ciclo de FKs: caemos a un orden arbitrario y avisamos.
        faltantes = [t for t in tablas if t not in orden]
        print(f"  AVISO: ciclo de llaves foraneas detectado en {faltantes}; "
              "se insertaran al final sin orden garantizado.")
        orden.extend(sorted(faltantes))
    return orden


def copiar_tabla(mysql_cur, pg_cur, tabla):
    # En MySQL los identificadores se citan con backticks.
    mysql_cur.execute(f"SELECT * FROM `{tabla}`")
    filas = mysql_cur.fetchall()
    if not filas:
        print(f"  {tabla}: 0 filas (omitida)")
        return 0

    columnas = [d[0] for d in mysql_cur.description]

    # Convertimos los blobs (bytes) al tipo binario que entiende psycopg2.
    filas_convertidas = []
    for fila in filas:
        filas_convertidas.append(
            tuple(
                psycopg2.Binary(v) if isinstance(v, (bytes, bytearray)) else v
                for v in fila
            )
        )

    insert = sql.SQL("INSERT INTO {} ({}) VALUES %s").format(
        sql.Identifier(tabla),
        sql.SQL(", ").join(map(sql.Identifier, columnas)),
    )
    execute_values(pg_cur, insert.as_string(pg_cur), filas_convertidas)
    print(f"  {tabla}: {len(filas)} filas copiadas")
    return len(filas)


def resetear_secuencias(pg_cur, tablas):
    print("Reseteando secuencias de IDs...")
    for tabla in tablas:
        pg_cur.execute(
            """
            SELECT column_name
            FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = %s
            """,
            (tabla,),
        )
        for (col,) in pg_cur.fetchall():
            pg_cur.execute("SELECT pg_get_serial_sequence(%s, %s)", (tabla, col))
            seq = pg_cur.fetchone()[0]
            if not seq:
                continue
            pg_cur.execute(
                sql.SQL("SELECT MAX({}) FROM {}").format(
                    sql.Identifier(col), sql.Identifier(tabla)
                )
            )
            maximo = pg_cur.fetchone()[0]
            if maximo is not None:
                pg_cur.execute("SELECT setval(%s, %s)", (seq, maximo))
                print(f"  {tabla}.{col} -> secuencia en {maximo}")


def main():
    if "USUARIO:PASSWORD@HOST" in POSTGRES_URL:
        sys.exit(
            "Configura POSTGRES_URL con la External Database URL de Render.\n"
            'Ej (PowerShell):  $env:POSTGRES_URL = '
            '"postgresql://user:pass@dpg-xxxx.oregon-postgres.render.com:5432/midb"'
        )

    print("Conectando a MySQL...")
    mysql_conn = pymysql.connect(**MYSQL_CONFIG)
    print("Conectando a PostgreSQL...")
    # sslmode=prefer: usa SSL si el servidor lo soporta (Render) y cae a
    # conexion normal si no (PostgreSQL local). Render exige SSL y con 'prefer'
    # se negocia automaticamente. Puedes forzarlo con la variable PGSSLMODE.
    sslmode = os.getenv("PGSSLMODE", "prefer")
    try:
        pg_conn = psycopg2.connect(POSTGRES_URL, sslmode=sslmode)
    except UnicodeDecodeError:
        # En Windows con PostgreSQL en español, un error de conexion (clave
        # incorrecta o base inexistente) llega en codificacion no-UTF8 y
        # psycopg2 no lo puede decodificar. El problema real es la conexion.
        mysql_conn.close()
        sys.exit(
            "No se pudo conectar a PostgreSQL: la conexion fue rechazada.\n"
            "Causas tipicas:\n"
            "  - Usuario o contrasena incorrectos en POSTGRES_URL.\n"
            "  - La base de datos indicada no existe.\n"
            "Verifica tu POSTGRES_URL. Si apuntabas a una Postgres local, "
            "revisa la clave y que la base exista; lo recomendable es apuntar "
            "directamente a la External Database URL de Render."
        )

    try:
        with mysql_conn.cursor() as mysql_cur, pg_conn.cursor() as pg_cur:
            db = MYSQL_CONFIG["database"]
            tablas = [t for t in obtener_tablas(mysql_cur, db)
                      if t not in TABLAS_A_OMITIR]
            edges = obtener_dependencias_fk(mysql_cur, db)
            orden = ordenar_por_dependencias(tablas, edges)

            print(f"\nTablas a migrar (en orden): {', '.join(orden)}\n")

            total = 0
            for tabla in orden:
                total += copiar_tabla(mysql_cur, pg_cur, tabla)

            resetear_secuencias(pg_cur, orden)

        pg_conn.commit()
        print(f"\nListo. {total} filas migradas correctamente.")
    except Exception:
        pg_conn.rollback()
        print("\nERROR: se revirtieron todos los cambios (rollback).")
        raise
    finally:
        mysql_conn.close()
        pg_conn.close()


if __name__ == "__main__":
    main()
