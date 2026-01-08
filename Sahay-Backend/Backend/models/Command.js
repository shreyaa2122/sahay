const mongoose = require('mongoose');
const CommandSchema = new mongoose.Schema({
    query: String,
    response: Object,
    timestamp: { type: Date, default: Date.now }
});
module.exports = mongoose.model('Command', CommandSchema);