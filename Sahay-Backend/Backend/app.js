const express = require('express');
const cors = require('cors');

// Import Routes
const commandRoutes = require('./routes/commandRoutes');
const appConfigRoutes = require('./routes/appConfigRoutes');

const app = express();

// Standard Middlewares
app.use(cors());
app.use(express.json());

// Register API Endpoints
app.use('/api/v1/commands', commandRoutes); // Everything related to AI and history
app.use('/api/v1/apps', appConfigRoutes);   // Everything related to app management

// Basic Health Check
app.get('/', (req, res) => {
    res.json({ message: "Sahay API is Live", version: "1.0.0" });
});

module.exports = app;