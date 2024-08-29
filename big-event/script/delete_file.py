import os
import sys

def delete_file(file_path):
    try:
        if os.path.exists(file_path):
            os.remove(file_path)
            print(f"Deleted file: {file_path}")
            return 0
        else:
            print(f"File not found: {file_path}")
            return 1
    except Exception as e:
        print(f"Error deleting file: {e}")
        return 1

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python delete_file.py <file_path>")
        sys.exit(1)

    file_path = sys.argv[1]
    exit_code = delete_file(file_path)
    sys.exit(exit_code)
