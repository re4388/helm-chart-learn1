// MongoDB initialization script
// This script creates a user for the demo database

db = db.getSiblingDB('demo');

// Create a user for the demo database
db.createUser({
  user: 'demo_user',
  pwd: 'demo_password',
  roles: [
    {
      role: 'readWrite',
      db: 'demo'
    }
  ]
});

// Create some initial collections (optional)
db.createCollection('posts');
db.createCollection('files');

print('Database initialization completed');