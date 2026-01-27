const Command = require('../models/Command');
const aiService = require('../services/aiService');

// 1. Process New Command (The Core "Brain" Logic)
exports.handleCommand = async (req, res) => {
    try {
        const { question } = req.body;
        
        // Call the AI service (Groq) to get the structured action
        const analysis = await aiService.processWithAI(question);

        // Save the interaction to MongoDB for history and analytics
        await Command.create({
            query: question,
            response: analysis
        });

        res.status(200).json(analysis);
    } catch (error) {
        console.error("AI Processing Error:", error);
        res.status(500).json({ 
            action: "reply", 
            text: "I'm having trouble thinking right now. Please try again." 
        });
    }
};

// 2. Get All History (Sorted by latest)
exports.getHistory = async (req, res) => {
    try {
        const history = await Command.find()
            .sort({ timestamp: -1 })
            .limit(50); // Fetching last 50 entries
        res.json(history);
    } catch (error) {
        res.status(500).json({ error: "Could not fetch history" });
    }
};

// 3. Get Advanced Stats (Analytics)
exports.getStats = async (req, res) => {
    try {
        const total = await Command.countDocuments();
        
        // Aggregation 1: Breakdown by action type (reply vs open_app vs call)
        const stats = await Command.aggregate([
            { $group: { _id: "$response.action", count: { $sum: 1 } } } 
        ]);

        // Aggregation 2: Find the most frequently used app
        const appStats = await Command.aggregate([
            { $match: { "response.action": "open_app" } },
            { $group: { _id: "$response.package_id", count: { $sum: 1 } } },
            { $sort: { count: -1 } },
            { $limit: 1 }
        ]);

        res.json({
            totalCommandsSent: total,
            actionBreakdown: stats,
            mostUsedApp: appStats.length > 0 ? appStats[0]._id : "No apps opened yet"
        });
    } catch (error) {
        console.error("Stats Error:", error);
        res.status(500).json({ error: "Stats calculation failed" });
    }
};

// 4. Delete Specific Command by ID
exports.deleteCommand = async (req, res) => {
    try {
        await Command.findByIdAndDelete(req.params.id);
        res.json({ message: "Entry deleted successfully" });
    } catch (error) {
        res.status(500).json({ error: "Delete failed" });
    }
};

// 5. Clear All History (Reset Data)
exports.clearAll = async (req, res) => {
    try {
        await Command.deleteMany({});
        res.json({ message: "All history has been cleared" });
    } catch (error) {
        res.status(500).json({ error: "Clear failed" });
    }
};