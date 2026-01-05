```

## API Endpoints

### Create User
```http
POST /api/users
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "securePassword123"
}

Response: 201 Created
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "isActive": true,
  "createdAt": "2024-01-05T10:30:00",
  "updatedAt": "2024-01-05T10:30:00"
}
```

### Get User by ID
```http
GET /api/users/1

Response: 200 OK
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "isActive": true,
  "createdAt": "2024-01-05T10:30:00",
  "updatedAt": "2024-01-05T10:30:00"
}
```

### Update User
```http
PUT /api/users/1
Content-Type: application/json

{
  "email": "newemail@example.com",
  "password": "newPassword123"
}

Response: 200 OK
```

### Deactivate User (Soft Delete)
```http
PATCH /api/users/1/deactivate

Response: 204 No Content
```

### Delete User (Hard Delete)
```http
DELETE /api/users/1

Response: 204 No Content
```

### Search Users
```http
GET /api/users/search?username=john

Response: 200 OK
[
  {
    "id": 1,
    "username": "johndoe",
    ...
  }
]
```

---
