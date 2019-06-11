var __extends = (this && this.__extends) || (function () {
    var extendStatics = Object.setPrototypeOf ||
        ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
        function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
define(["require", "exports", "interpreter.aRobotBehaviour", "interpreter.constants", "interpreter.util"], function (require, exports, interpreter_aRobotBehaviour_1, C, U) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var RobotWeDoBehaviour = (function (_super) {
        __extends(RobotWeDoBehaviour, _super);
        function RobotWeDoBehaviour(btInterfaceFct, toDisplayFct) {
            var _this = _super.call(this) || this;
            _this.wedo = {};
            _this.tiltMode = {
                UP: '3.0',
                DOWN: '9.0',
                BACK: '5.0',
                FRONT: '7.0',
                NO: '0.0'
            };
            _this.btInterfaceFct = btInterfaceFct;
            _this.toDisplayFct = toDisplayFct;
            _this.timers = {};
            _this.timers['start'] = Date.now();
            U.loggingEnabled(true, true);
            return _this;
        }
        RobotWeDoBehaviour.prototype.update = function (data) {
            U.info('update ' + data);
            if (data.target !== "wedo") {
                return;
            }
            switch (data.type) {
                case "connect":
                    if (data.state == "connected") {
                        this.wedo[data.brickid] = {};
                        this.wedo[data.brickid]["brickname"] = data.brickname.replace(/\s/g, '').toUpperCase();
                        // for some reason we do not get the inital state of the button, so here it is hardcoded
                        this.wedo[data.brickid]["button"] = 'false';
                    }
                    else if (data.state == "disconnected") {
                        delete this.wedo[data.brickid];
                    }
                    break;
                case "didAddService":
                    if (data.state == "connected") {
                        if (data.id && data.sensor) {
                            this.wedo[data.brickid][data.id] = {};
                            this.wedo[data.brickid][data.id][data.sensor.replace(/\s/g, '').toLowerCase()] = '';
                        }
                        else if (data.id && data.actuator) {
                            this.wedo[data.brickid][data.id] = {};
                            this.wedo[data.brickid][data.id][data.actuator.replace(/\s/g, '').toLowerCase()] = '';
                        }
                        else if (data.sensor) {
                            this.wedo[data.brickid][data.sensor.replace(/\s/g, '').toLowerCase()] = '';
                        }
                        else {
                            this.wedo[data.brickid][data.actuator.replace(/\s/g, '').toLowerCase()] = '';
                        }
                    }
                    break;
                case "didRemoveService":
                    if (data.id) {
                        delete this.wedo[data.brickid][data.id];
                    }
                    else if (data.sensor) {
                        delete this.wedo[data.brickid][data.sensor.replace(/\s/g, '').toLowerCase()];
                    }
                    else {
                        delete this.wedo[data.brickid][data.actuator.replace(/\s/g, '').toLowerCase()];
                    }
                    break;
                case "update":
                    if (data.id) {
                        this.wedo[data.brickid][data.id][data.sensor.replace(/\s/g, '').toLowerCase()] = data.state;
                    }
                    else {
                        this.wedo[data.brickid][data.sensor.replace(/\s/g, '').toLowerCase()] = data.state;
                    }
                    break;
                default:
                    // TODO think about what could happen here.
                    break;
            }
            U.info(this.wedo);
        };
        RobotWeDoBehaviour.prototype.getSensorValue = function (brickid, sensor, id, slot) {
            switch (sensor) {
                case "tiltsensor":
                    if (slot === "ANY") {
                        return this.wedo[brickid][id][sensor] !== this.tiltMode.NO;
                    }
                    else {
                        return this.wedo[brickid][id][sensor] === this.tiltMode[slot];
                    }
                case "motionsensor":
                    return parseInt(this.wedo[brickid][id][sensor]);
                case "button":
                    return this.wedo[brickid][sensor] === "true";
            }
        };
        RobotWeDoBehaviour.prototype.getConnectedBricks = function () {
            var brickids = [];
            for (var brickid in this.wedo) {
                if (this.wedo.hasOwnProperty(brickid)) {
                    brickids.push(brickid);
                }
            }
            return brickids;
        };
        RobotWeDoBehaviour.prototype.getBrickIdByName = function (name) {
            for (var brickid in this.wedo) {
                if (this.wedo.hasOwnProperty(brickid)) {
                    if (this.wedo[brickid].brickname === name.toUpperCase()) {
                        return brickid;
                    }
                }
            }
            return null;
        };
        RobotWeDoBehaviour.prototype.getBrickById = function (id) {
            return this.wedo[id];
        };
        RobotWeDoBehaviour.prototype.clearDisplay = function () {
            U.debug('clear display');
            this.toDisplayFct({ "clear": true });
        };
        RobotWeDoBehaviour.prototype.getSample = function (s, name, port, sensor, slot) {
            var robotText = 'robot: ' + name + ', port: ' + port;
            U.info(robotText + ' getsample from ' + sensor);
            U.info(' state ' + this.wedo);
            var sensorName;
            switch (sensor) {
                case "infrared":
                    sensorName = "motionsensor";
                    break;
                case "gyro":
                    sensorName = "tiltsensor";
                    break;
                case "buttons":
                    sensorName = "button";
                    break;
                case C.TIMER:
                    s.push(this.timerGet(port));
                    return;
                default:
                    throw 'invalid get sample for ' + name + ' - ' + port + ' - ' + sensor + ' - ' + slot;
            }
            var brickid = this.getBrickIdByName(name);
            s.push(this.getSensorValue(brickid, sensorName, port, slot));
        };
        RobotWeDoBehaviour.prototype.timerReset = function (port) {
            this.timers[port] = Date.now();
            U.debug('timerReset for ' + port);
        };
        RobotWeDoBehaviour.prototype.timerGet = function (port) {
            var now = Date.now();
            var startTime = this.timers[port];
            if (startTime === undefined) {
                startTime = this.timers['start'];
            }
            var delta = now - startTime;
            U.debug('timerGet for ' + port + ' returned ' + delta);
            return delta;
        };
        RobotWeDoBehaviour.prototype.ledOnAction = function (name, port, color) {
            var brickid = this.getBrickIdByName(name);
            var robotText = 'robot: ' + name + ', port: ' + port;
            U.debug(robotText + ' led on color ' + color);
            var cmd = { 'target': 'wedo', 'type': 'command', 'actuator': 'light', 'brickid': brickid, 'color': color };
            this.btInterfaceFct(cmd);
        };
        RobotWeDoBehaviour.prototype.statusLightOffAction = function (name, port) {
            var brickid = this.getBrickIdByName(name);
            var robotText = 'robot: ' + name + ', port: ' + port;
            U.debug(robotText + ' led off');
            var cmd = { 'target': 'wedo', 'type': 'command', 'actuator': 'light', 'brickid': brickid, 'color': 0 };
            this.btInterfaceFct(cmd);
        };
        RobotWeDoBehaviour.prototype.toneAction = function (name, frequency, duration) {
            var brickid = this.getBrickIdByName(name); // TODO: better style
            var robotText = 'robot: ' + name;
            U.debug(robotText + ' piezo: ' + ', frequency: ' + frequency + ', duration: ' + duration);
            var cmd = { 'target': 'wedo', 'type': 'command', 'actuator': 'piezo', 'brickid': brickid, 'frequency': frequency, 'duration': duration };
            this.btInterfaceFct(cmd);
        };
        RobotWeDoBehaviour.prototype.motorOnAction = function (name, port, duration, speed) {
            var brickid = this.getBrickIdByName(name); // TODO: better style
            var robotText = 'robot: ' + name + ', port: ' + port;
            var durText = duration === -1 ? ' w.o. duration' : (' for ' + duration + ' msec');
            U.debug(robotText + ' motor speed ' + speed + durText);
            var cmd = { 'target': 'wedo', 'type': 'command', 'actuator': 'motor', 'brickid': brickid, 'action': 'on', 'id': port, 'direction': speed < 0 ? 1 : 0, 'power': Math.abs(speed) };
            this.btInterfaceFct(cmd);
            return 0;
        };
        RobotWeDoBehaviour.prototype.motorStopAction = function (name, port) {
            var brickid = this.getBrickIdByName(name); // TODO: better style
            var robotText = 'robot: ' + name + ', port: ' + port;
            U.debug(robotText + ' motor stop');
            var cmd = { 'target': 'wedo', 'type': 'command', 'actuator': 'motor', 'brickid': brickid, 'action': 'stop', 'id': port };
            this.btInterfaceFct(cmd);
        };
        RobotWeDoBehaviour.prototype.showTextAction = function (text) {
            var showText = "" + text;
            U.debug('***** show "' + showText + '" *****');
            this.toDisplayFct({ "show": showText });
            return 0;
        };
        RobotWeDoBehaviour.prototype.showImageAction = function (_text, _mode) {
            U.debug('***** show image not supported by WeDo *****');
            return 0;
        };
        RobotWeDoBehaviour.prototype.close = function () {
            var ids = this.getConnectedBricks();
            for (var id in ids) {
                if (ids.hasOwnProperty(id)) {
                    var name = this.getBrickById(ids[id]).brickname;
                    this.motorStopAction(name, 1);
                    this.motorStopAction(name, 2);
                    this.ledOnAction(name, 99, 3);
                }
            }
        };
        return RobotWeDoBehaviour;
    }(interpreter_aRobotBehaviour_1.ARobotBehaviour));
    exports.RobotWeDoBehaviour = RobotWeDoBehaviour;
});
