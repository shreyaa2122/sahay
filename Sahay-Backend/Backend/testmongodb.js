require('dotenv').config({ path: './config.env' });
const { MongoClient } = require('mongodb');

async function testConnection() {
    console.log('🔍 Testing MongoDB Atlas connection...');
    console.log('Connection string (hidden password):', 
        process.env.MONGODB_URI?.replace(/:[^:@]+@/, ':****@'));
    
    const client = new MongoClient(process.env.MONGODB_URI);
    
    try {
        await client.connect();
        console.log('✅ Connected to MongoDB Atlas!');
        
        // List databases
        const adminDb = client.db().admin();
        const databases = await adminDb.listDatabases();
        
        console.log('\n📚 Available databases:');
        databases.databases.forEach(db => {
            console.log(`   - ${db.name}`);
        });
        
        // Try to access your specific database
        const dbName = process.env.MONGODB_URI.match(/\/([^/?]+)/)?.[1];
        if (dbName) {
            console.log(`\n🔍 Trying to access database: ${dbName}`);
            const db = client.db(dbName);
            const collections = await db.listCollections().toArray();
            
            if (collections.length > 0) {
                console.log('✅ Database accessible. Collections:');
                collections.forEach(col => console.log(`   - ${col.name}`));
            } else {
                console.log('ℹ️ Database exists but has no collections');
            }
        }
        
    } catch (error) {
        console.error('❌ Connection failed:', error.message);
        console.log('\n🔧 Common solutions:');
        console.log('1. Check your IP is whitelisted in MongoDB Atlas');
        console.log('2. Make sure you have "Network Access" configured');
        console.log('3. Verify username/password are correct');
        console.log('4. Check if the database name exists');
    } finally {
        await client.close();
    }
}

testConnection();