const Groq = require("groq-sdk");
const AppConfig = require('../models/AppConfig');

// Initialize Groq with your API key from config.env
const groq = new Groq({
    apiKey: process.env.GROQ_API_KEY,
});

exports.processWithAI = async (userInput) => {
    try {
        console.log("🚀 Processing with Groq:", userInput);
        
        // 1. Get apps from database (optional)
        let apps = [];
        try {
            apps = await AppConfig.find({}, 'name packageId keywords');
            console.log("📱 Found", apps.length, "apps in DB");
        } catch (dbError) {
            console.log("⚠️ Database error, using default apps list");
            apps = getDefaultApps();
        }
        
        console.log("📤 Sending to Groq...");
        
        // 2. Call Groq Chat Completion with the latest model
        const response = await groq.chat.completions.create({
            model: "llama-3.1-8b-instant", // Updated model name
            messages: [
                {
                    role: "system",
                    content: `You are Sahay, an Android voice assistant. 
                    Your goal is to understand user commands and respond ONLY in JSON format.
                    
                    Available apps for "open_app" action:
                    ${JSON.stringify(apps)}
                    
                    Rules:
                    1. If user wants to open an app, respond with "open_app" action and provide the correct package_id.
                    2. If user wants to call someone, respond with "call" action and provide contact name/number.
                    3. For anything else, respond with "reply" action.
                    
                    Respond ONLY with this JSON structure:
                    {
                        "action": "open_app" | "call" | "reply",
                        "text": "User-friendly response message here",
                        "package_id": "app.package.name or null",
                        "contact": "contact name or null"
                    }`
                },
                { role: "user", content: userInput }
            ],
            temperature: 0.7,
            // Forces Groq to return a structured JSON object
            response_format: { type: "json_object" } 
        });

        const aiResponse = JSON.parse(response.choices[0].message.content);
        console.log("📥 Groq response:", aiResponse);
        
        return aiResponse;
        
    } catch (error) {
        console.error('❌ Groq Error:', error.message);
        
        // Fallback to simple local logic if the API fails
        return getFallbackResponse(userInput);
    }
};

/**
 * Basic logic to handle commands if the AI API is offline
 */
function getFallbackResponse(userInput) {
    const input = userInput.toLowerCase();
    
    if (input.includes("whatsapp")) {
        return {
            action: "open_app",
            text: "Opening WhatsApp...",
            package_id: "com.whatsapp",
            contact: null
        };
    } else if (input.includes("call")) {
        let contact = "Contact";
        if (input.includes("mom")) contact = "Mom";
        if (input.includes("dad")) contact = "Dad";
        
        return {
            action: "call",
            text: `Calling ${contact}...`,
            contact: contact,
            package_id: null
        };
    } else if (input.includes("gallery") || input.includes("photos")) {
        return {
            action: "open_app",
            text: "Opening Gallery...",
            package_id: "com.google.android.apps.photos",
            contact: null
        };
    } else {
        return {
            action: "reply",
            text: `You said: "${userInput}". I can help with: WhatsApp, Calls, and opening Gallery.`,
            package_id: null,
            contact: null
        };
    }
}

/**
 * Default app list in case MongoDB is empty
 */
function getDefaultApps() {
    return [
        { name: "whatsapp", packageId: "com.whatsapp", keywords: ["whatsapp", "chat"] },
        { name: "gallery", packageId: "com.google.android.apps.photos", keywords: ["gallery", "photos"] },
        { name: "chrome", packageId: "com.android.chrome", keywords: ["chrome", "browser"] },
        { name: "blinkit", packageId: "com.grofers.customerapp", keywords: ["blinkit", "grofers"] }
    ];
}
