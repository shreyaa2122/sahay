const mongoose = require('mongoose');
const AppConfigSchema = new mongoose.Schema({
    name: { type: String, required: true, unique: true }, // e.g. "whatsapp"
    packageId: { type: String, required: true },         // e.g. "com.whatsapp"
    keywords: [String]                                   // e.g. ["chat", "messages"]
});
module.exports = mongoose.model('AppConfig', AppConfigSchema);