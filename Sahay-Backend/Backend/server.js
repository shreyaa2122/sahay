// 1. Load environment variables FIRST
require('dotenv').config({ path: './config.env' });

const app = require('./app');
const connectDB = require('./config/dbconfig'); // Make sure this path matches your structure

// 2. Connect to Database
connectDB();

// 3. Start Server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`🚀 Sahay Backend running on http://localhost:${PORT}`);
});