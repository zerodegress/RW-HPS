#!/usr/bin/env node
const fs = require("fs")
const path = require("path")

fs.copyFileSync(path.join(__dirname, "src", "rhino.d.ts"), path.join(__dirname, "types", "rhino.d.ts"))