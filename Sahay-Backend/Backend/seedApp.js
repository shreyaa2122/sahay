// seed.js
require('dotenv').config({ path: './config.env' });
const mongoose = require('mongoose');
const AppConfig = require('./models/AppConfig');

async function seedApps() {
    try {
        await mongoose.connect(process.env.MONGODB_URI);
        await AppConfig.deleteMany({});
        
        const apps = [
            { name: "whatsapp", packageId: "com.whatsapp", keywords: ["chat", "message"] },
            { name: "blinkit", packageId: "com.grofers.customerapp", keywords: ["grocery", "blinkit", "grofers"] },
            { name: "youtube", packageId: "com.google.android.youtube", keywords: ["video", "watch", "yt"] },
            { name: "instagram", packageId: "com.instagram.android", keywords: ["insta", "reels", "photos"] },
            { name: "chrome", packageId: "com.android.chrome", keywords: ["browser", "google", "search"] },
            { name: "zomato", packageId: "com.application.zomato", keywords: ["food", "order", "delivery"] },
            { name: "camera", packageId: "com.,  android.camera", keywords: ["photo", "selfie"] },
            {name : "gallery",  packageId : "com.google.android.apps.photos", keywords : ["album" , "screenshot", "videos"]}
        ];
        
        await AppConfig.insertMany(apps);
        console.log('✅ Advanced App Registry Seeded!');
        process.exit(0);
    } catch (e) { console.error(e); process.exit(1); }
}
seedApps();