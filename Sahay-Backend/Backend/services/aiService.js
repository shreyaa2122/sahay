const Groq = require("groq-sdk");
const AppConfig = require('../models/AppConfig');

// Initialize Groq
const groq = new Groq({ apiKey: process.env.GROQ_API_KEY });

exports.processWithAI = async (userInput) => {
    try {
        console.log("🚀 User said:", userInput);

        const apps = await AppConfig.find({}, 'name packageId keywords');
        
        const systemPrompt = `
            You are Sahay, a professional Android voice assistant for elderly people. 
            Respond ONLY in valid JSON.

            SUPPORTED APPS: ${JSON.stringify(apps)}

            TASKS & JSON FORMAT:
            1. OPEN APP: {"action": "open_app", "package_id": "com.pkg", "text": "Opening..."}
            2. CALL: {"action": "call", "contact": "Name/Number", "text": "Calling..."}
            3. SET ALARM/REMINDER: For medicine or time-based alerts.
               - JSON: {"action": "set_alarm", "hour": 24_hr_format, "minute": 0_60, "message": "Reason", "text": "Setting alarm..."}
            4. ADD TO CART: For shopping requests.
               - JSON: {"action": "add_item", "item": "name", "quantity": "amount", "text": "Adding to cart..."}
            5. CHAT: {"action": "reply", "text": "Helpful response"}

            RULES:
            - If user says "Remind me to take medicine at 8 PM", hour=20, minute=0, message="Take medicine".
            - If user says "Add milk to my list", item="milk".
            - Output MUST be raw JSON. No markdown.
        `;

        const response = await groq.chat.completions.create({
            model: "llama-3.1-8b-instant",
            messages: [
                { role: "system", content: systemPrompt },
                { role: "user", content: userInput }
            ],
            temperature: 0.1,
            response_format: { type: "json_object" }
        });

        const aiJson = JSON.parse(response.choices[0].message.content);
        console.log("✅ AI Response:", aiJson);
        return aiJson;

    } catch (error) {
        console.error('❌ AI Brain Error:', error.message);
        return { action: "reply", text: "My brain is a bit slow. Is the internet working?" };
    }
};