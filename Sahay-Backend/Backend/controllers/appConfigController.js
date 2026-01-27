const AppConfig = require('../models/AppConfig');

exports.getAllApps = async (req, res) => {
    const apps = await AppConfig.find();
    res.json(apps);
};

exports.addApp = async (req, res) => {
    const newApp = await AppConfig.create(req.body);
    res.status(201).json(newApp);
};

exports.removeApp = async (req, res) => {
    await AppConfig.findByIdAndDelete(req.params.id);
    res.json({ message: "App removed" });
};