from flask import Flask, request, jsonify
import mysql.connector # Or import pymysql.connections as db_connection
from werkzeug.security import generate_password_hash, check_password_hash

app = Flask(__name__)

# Database Configuration (Replace with your XAMPP MySQL credentials)
DB_CONFIG = {
    'host': 'localhost', # Or your local network IP if testing on a different device
    'user': 'root',      # Default XAMPP MySQL user
    'password': '',      # Default XAMPP MySQL password
    'database': 'digital_badge_db'
}

def get_db_connection():
    """Establishes a database connection."""
    try:
        conn = mysql.connector.connect(**DB_CONFIG) # Or pymysql.connect(**DB_CONFIG)
        return conn
    except mysql.connector.Error as err: # Or pymysql.err.OperationalError
        print(f"Database connection error: {err}")
        return None

@app.route('/')
def index():
    return "Digital Badge System Backend is running!"

# --- Authentication ---

@app.route('/register', methods=['POST'])
def register_user():
    data = request.json
    username = data.get('username')
    password = data.get('password') # Get plain password from request
    email = data.get('email')
    full_name = data.get('full_name')

    if not username or not password:
        return jsonify({'message': 'Username and password are required'}), 400

    # Hash the password
    hashed_password = generate_password_hash(password, method='pbkdf2:sha256') # Use a strong hashing method

    conn = get_db_connection()
    if conn is None:
        return jsonify({'message': 'Database error'}), 500

    cursor = conn.cursor()
    try:
        # Check if username or email already exists
        cursor.execute("SELECT user_id FROM users WHERE username = %s OR email = %s", (username, email))
        if cursor.fetchone():
            return jsonify({'message': 'Username or email already exists'}), 409

        # Insert new user
        sql = "INSERT INTO users (username, password_hash, email, full_name) VALUES (%s, %s, %s, %s)"
        cursor.execute(sql, (username, hashed_password, email, full_name))
        conn.commit()
        return jsonify({'message': 'User registered successfully'}), 201
    except Exception as e:
        conn.rollback()
        print(f"Registration error: {e}")
        return jsonify({'message': 'An error occurred during registration'}), 500
    finally:
        cursor.close()
        conn.close()

@app.route('/login', methods=['POST'])
def login_user():
    data = request.json
    username = data.get('username')
    password = data.get('password') # Get plain password

    if not username or not password:
        return jsonify({'message': 'Username and password are required'}), 400

    conn = get_db_connection()
    if conn is None:
        return jsonify({'message': 'Database error'}), 500

    cursor = conn.cursor(dictionary=True) # Use dictionary=True to fetch rows as dicts
    try:
        # Find the user by username
        cursor.execute("SELECT user_id, password_hash FROM users WHERE username = %s", (username,))
        user = cursor.fetchone()

        if user and check_password_hash(user['password_hash'], password):
            # Password is correct
            # In a real app, you'd generate a token (JWT) here and return it
            # For this example, we'll just return success and user_id
            return jsonify({'message': 'Login successful', 'user_id': user['user_id']}), 200
        else:
            return jsonify({'message': 'Invalid username or password'}), 401
    except Exception as e:
        print(f"Login error: {e}")
        return jsonify({'message': 'An error occurred during login'}), 500
    finally:
        cursor.close()
        conn.close()

# --- User Profile ---

@app.route('/profile/<int:user_id>', methods=['GET'])
def get_user_profile(user_id):
    conn = get_db_connection()
    if conn is None:
        return jsonify({'message': 'Database error'}), 500

    cursor = conn.cursor(dictionary=True)
    try:
        # Get user details
        cursor.execute("SELECT user_id, username, email, full_name, profile_bio FROM users WHERE user_id = %s", (user_id,))
        user = cursor.fetchone()

        if not user:
            return jsonify({'message': 'User not found'}), 404

        # Get user's badges
        sql_badges = """
            SELECT b.badge_id, b.badge_name, b.description, b.icon_url, ub.earned_date
            FROM user_badges ub
            JOIN badges b ON ub.badge_id = b.badge_id
            WHERE ub.user_id = %s
        """
        cursor.execute(sql_badges, (user_id,))
        user_badges = cursor.fetchall()

        # Get user's certification progress
        sql_certs = """
            SELECT c.cert_id, c.cert_name, c.description, c.required_badges, ucp.status, ucp.completion_date
            FROM user_cert_progress ucp
            JOIN certifications c ON ucp.cert_id = c.cert_id
            WHERE ucp.user_id = %s
        """
        cursor.execute(sql_certs, (user_id,))
        user_certs = cursor.fetchall()

        profile_data = {
            'user': user,
            'badges': user_badges,
            'certifications': user_certs
        }

        return jsonify(profile_data), 200
    except Exception as e:
        print(f"Profile error: {e}")
        return jsonify({'message': 'An error occurred fetching profile'}), 500
    finally:
        cursor.close()
        conn.close()

# --- Badge Listing ---

@app.route('/badges', methods=['GET'])
def get_all_badges():
    conn = get_db_connection()
    if conn is None:
        return jsonify({'message': 'Database error'}), 500

    cursor = conn.cursor(dictionary=True)
    try:
        # Get all available badges
        cursor.execute("SELECT badge_id, badge_name, description, icon_url FROM badges")
        all_badges = cursor.fetchall()
        return jsonify(all_badges), 200
    except Exception as e:
        print(f"Badges listing error: {e}")
        return jsonify({'message': 'An error occurred fetching badges'}), 500
    finally:
        cursor.close()
        conn.close()

@app.route('/badges/<int:badge_id>', methods=['GET'])
def get_badge_details(badge_id):
    conn = get_db_connection()
    if conn is None:
        return jsonify({'message': 'Database error'}), 500

    cursor = conn.cursor(dictionary=True)
    try:
        # Get badge details
        cursor.execute("SELECT badge_id, badge_name, description, icon_url, criteria FROM badges WHERE badge_id = %s", (badge_id,))
        badge = cursor.fetchone()

        if not badge:
            return jsonify({'message': 'Badge not found'}), 404

        return jsonify(badge), 200
    except Exception as e:
        print(f"Badge details error: {e}")
        return jsonify({'message': 'An error occurred fetching badge details'}), 500
    finally:
        cursor.close()
        conn.close()

# --- Certification Progress ---
# This is partly covered in get_user_profile, but you might want a dedicated endpoint
# For simplicity, let's assume get_user_profile provides the necessary cert info for now.
# A dedicated endpoint might be needed if you have more complex certification logic.

# --- Share Achievements ---
# This is primarily a frontend action (opening sharing intents).
# The backend might be used to generate a shareable link or retrieve details for sharing.
# Example: Endpoint to get details of a specific user achievement (badge or certification)

@app.route('/achievements/<int:user_badge_or_cert_id>', methods=['GET'])
def get_achievement_details_for_sharing(user_badge_or_cert_id):
    # This function would need logic to determine if the ID refers to a user_badge or user_cert_progress
    # and fetch appropriate data. This is complex and depends on how you structure shareable links.
    # For now, let's just return a placeholder or error.
    return jsonify({'message': 'Sharing endpoint requires more specific implementation based on achievement type'}), 501 # Not Implemented


# --- Settings ---
# Settings might involve updating user profile details (name, bio, etc.)
# Let's add an endpoint for updating profile.

@app.route('/profile/<int:user_id>', methods=['PUT'])
def update_user_profile(user_id):
    data = request.json
    full_name = data.get('full_name')
    profile_bio = data.get('profile_bio')
    # Add other updatable fields like email, password (needs old password verification)

    conn = get_db_connection()
    if conn is None:
        return jsonify({'message': 'Database error'}), 500

    cursor = conn.cursor()
    try:
        # You'd typically add authentication here to ensure the requesting user_id matches the token/session user_id
        # For simplicity, we'll just update the user_id provided in the URL

        sql = "UPDATE users SET "
        updates = []
        values = []

        if full_name is not None:
            updates.append("full_name = %s")
            values.append(full_name)
        if profile_bio is not None:
            updates.append("profile_bio = %s")
            values.append(profile_bio)

        if not updates:
            return jsonify({'message': 'No update data provided'}), 400

        sql += ", ".join(updates) + " WHERE user_id = %s"
        values.append(user_id)

        cursor.execute(sql, tuple(values))
        conn.commit()

        if cursor.rowcount == 0:
             return jsonify({'message': 'User not found or no changes made'}), 404 # Or 200 if no changes are okay

        return jsonify({'message': 'Profile updated successfully'}), 200
    except Exception as e:
        conn.rollback()
        print(f"Profile update error: {e}")
        return jsonify({'message': 'An error occurred updating profile'}), 500
    finally:
        cursor.close()
        conn.close()


if __name__ == '__main__':
    # To run on your local network so Android can access it:
    # Find your local IP address (e.g., using ipconfig on Windows, ifconfig on macOS/Linux)
    # Replace '0.0.0.0' with your local IP if running on a separate machine than the Android emulator/device.
    # '0.0.0.0' makes the server accessible externally (on your local network).
    # Debug=True is useful for development, turn off in production.
    app.run(debug=True, host='0.0.0.0', port=5000) # Use port 5000 or another available port