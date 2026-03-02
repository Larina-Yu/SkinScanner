import psycopg2
import os
import shutil

# 1️. PostgreSQL Connection

DB_HOST = "localhost"
DB_PORT = 5433
DB_NAME = "SkinScannerDB"
DB_USER = "postgres"
DB_PASS = "databases"

def create_connection():
    """Connects to PostgreSQL and returns the connection object."""
    try:
        conn = psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            dbname=DB_NAME,
            user=DB_USER,
            password=DB_PASS
        )
        print("Connected to PostgreSQL!")
        return conn
    except Exception as e:
        print("Connection error:", e)
        return None

conn = create_connection()
cursor = conn.cursor()


# 2️. Creating Tables

def create_tables(cursor):
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS users (
            user_id VARCHAR(128) PRIMARY KEY,  -- Firebase UID
            username VARCHAR(50),
            email VARCHAR(100),
            date_joined TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS images (
            image_id SERIAL PRIMARY KEY,
            user_id VARCHAR(128) REFERENCES users(user_id) ON DELETE CASCADE,
            image_url TEXT NOT NULL,  -- Firebase storage URL
            prediction VARCHAR(50),
            confidence FLOAT,
            skin_tone_ita FLOAT,
            upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
    """)
    print("Tables created or already exist.")

create_tables(cursor)
conn.commit()


# 3️. Adding User

def add_user(cursor, conn, username, email, password):
    try:
        cursor.execute("SELECT user_id FROM users WHERE username = %s;", (username,))
        result = cursor.fetchone()
        if result:
            print(f"User '{username}' already exists (ID: {result[0]})")
            return result[0]
        cursor.execute("""
            INSERT INTO users (username, email, password)
            VALUES (%s, %s, %s)
            RETURNING user_id;
        """, (username, email, password))
        user_id = cursor.fetchone()[0]
        conn.commit()
        print(f"User '{username}' added (ID: {user_id})")
        return user_id
    except Exception as e:
        conn.rollback()
        print("Error adding user:", e)
        return None


# 4️. Adding Image Metadata

IMAGE_DIR = os.path.join(os.getcwd(), "user_images")
os.makedirs(IMAGE_DIR, exist_ok=True)

def add_image(cursor, conn, user_id, source_path, lesion_type=None):
    """Copies image to local folder and stores metadata in DB."""
    try:
        filename = os.path.basename(source_path)
        dest_path = os.path.join(IMAGE_DIR, filename)
        shutil.copy(source_path, dest_path)

        # Store only the filename in the DB
        cursor.execute("""
            INSERT INTO images (user_id, image_url, lesion_type)
            VALUES (%s, %s, %s);
        """, (user_id, filename, lesion_type))
        conn.commit()
        print(f"Image '{filename}' added for user ID {user_id}")
    except Exception as e:
        conn.rollback()
        print("Error adding image:", e)

# 5️. Get User Images

def get_user_images(cursor, username):
    """Fetch all images for a given username."""
    cursor.execute("""
        SELECT images.filename, images.lesion_type, images.upload_date
        FROM images
        JOIN users ON images.user_id = users.user_id
        WHERE users.username = %s;
    """, (username,))
    return cursor.fetchall()


# 6️. Example Usage

#if __name__ == "__main__":
    # Adding a test user
    #user_id = add_user(cursor, conn, "test_user", "test@example.com", "password123")

    # Adding a test image 
    #add_image(cursor, conn, user_id, r"D:\Final Year Project\ImageProcessing\Images\Melanoma_Dark_1.jpeg", lesion_type="unknown")

    # Get and display all images for this user
    #images = get_user_images(cursor, "test_user")
    #print("\nUser Images:")
    #for img in images:
        #print(img)
