from flask import Flask, request, jsonify, send_from_directory
from flask_cors import CORS
import psycopg2
import os
import shutil

# ------------------ PostgreSQL Config ------------------
DB_HOST = "localhost"
DB_PORT = 5433
DB_NAME = "SkinScannerDB"
DB_USER = "postgres"
DB_PASS = "databases"

def create_connection():
    try:
        conn = psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            dbname=DB_NAME,
            user=DB_USER,
            password=DB_PASS
        )
        return conn
    except Exception as e:
        print("DB connection error:", e)
        return None

# ------------------ Flask App ------------------
app = Flask(__name__)
CORS(app)

conn = create_connection()
cursor = conn.cursor()

IMAGE_DIR = os.path.join(os.getcwd(), "user_images")
os.makedirs(IMAGE_DIR, exist_ok=True)

# ------------------ Helper Functions ------------------
def get_or_create_user(username, email, password):
    """Return user_id, create if not exists"""
    cursor.execute("SELECT user_id FROM users WHERE username=%s;", (username,))
    result = cursor.fetchone()
    if result:
        return result[0]
    cursor.execute(
        "INSERT INTO users (username, email, password) VALUES (%s,%s,%s) RETURNING user_id;",
        (username, email, password)
    )
    user_id = cursor.fetchone()[0]
    conn.commit()
    return user_id

def add_image_metadata(user_id, filename, lesion_type=None):
    cursor.execute(
        "INSERT INTO images (user_id, filename, lesion_type) VALUES (%s,%s,%s);",
        (user_id, filename, lesion_type)
    )
    conn.commit()

# ------------------ API Routes ------------------

@app.route("/images/<filename>")
def serve_image(filename):
    return send_from_directory(IMAGE_DIR, filename)

@app.route("/register", methods=["POST"])
def register_user():
    data = request.json
    username = data.get("username")
    email = data.get("email")
    password = data.get("password")
    if not all([username, email, password]):
        return jsonify({"error": "Missing fields"}), 400
    user_id = get_or_create_user(username, email, password)
    return jsonify({"user_id": user_id})

@app.route("/upload_image", methods=["POST"])
def upload_image():
    try:
        print("Upload request received")
        print("Form data:", request.form)
        print("Files:", request.files)

        if "file" not in request.files:
            return jsonify({"error": "No file provided"}), 400

        file = request.files["file"]
        user_id = request.form.get("user_id")
        lesion_type = request.form.get("lesion_type")

        if not user_id:
            return jsonify({"error": "No user_id provided"}), 400

        # Ensure user exists
        cursor.execute("SELECT user_id FROM users WHERE user_id=%s;", (user_id,))
        existing_user = cursor.fetchone()
        if not existing_user:
            # Automatically create minimal account if missing
            cursor.execute(
                "INSERT INTO users (user_id, username, email, password) VALUES (%s, %s, %s, %s);",
                (user_id, user_id, "firebase_user", "firebase_auth")
            )
            conn.commit()

        # Save file
        filename = file.filename
        save_path = os.path.join(IMAGE_DIR, filename)
        file.save(save_path)

        # Store filename only in DB
        cursor.execute(
            "INSERT INTO images (user_id, filename, lesion_type) VALUES (%s, %s, %s);",
            (user_id, filename, lesion_type)
        )
        conn.commit()

        print(f"Saved image '{filename}' for user {user_id}")
        return jsonify({"message": "Image uploaded successfully", "filename": filename})

    except Exception as e:
        conn.rollback()
        print("Error uploading image:", e)
        return jsonify({"error": str(e)}), 500


@app.route("/get_images/<user_id>", methods=["GET"])
def get_images(user_id):
    try:
        cursor.execute(
            "SELECT filename, lesion_type, upload_date FROM images WHERE user_id=%s;",
            (user_id,)
        )
        rows = cursor.fetchall()
        
        result = [
            {"filename": f, "lesion_type": l, "upload_date": str(d)}
            for f, l, d in rows
        ]

        return jsonify(result)
    except Exception as e:
        conn.rollback()
        return jsonify({"error": str(e)}), 500
    
print("Image folder path:", IMAGE_DIR)

with open(r"d:\Final Year Project\SQL\user_images\test.txt", "w") as f:
    f.write("test")



# ------------------ Run Server ------------------
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
