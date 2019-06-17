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
    var RobotMbedBehaviour = (function (_super) {
        __extends(RobotMbedBehaviour, _super);
        function RobotMbedBehaviour() {
            var _this = _super.call(this) || this;
            U.loggingEnabled(false, false);
            return _this;
        }
        RobotMbedBehaviour.prototype.getSample = function (s, name, sensor, port, slot) {
            var robotText = 'robot: ' + name + ', port: ' + port;
            U.debug(robotText + ' getsample from ' + sensor);
            var sensorName = sensor;
            if (sensorName == C.TIMER) {
                s.push(this.timerGet(port));
            }
            else {
                s.push(this.getSensorValue(sensorName, port));
            }
        };
        RobotMbedBehaviour.prototype.getSensorValue = function (sensorName, mode) {
            var sensor = this.hardwareState.sensors[sensorName];
            if (sensor === undefined) {
                return "undefined";
            }
            if (mode != undefined) {
                var v = sensor[mode];
                if (v === undefined) {
                    return "undefined";
                }
                else {
                    return v;
                }
            }
            return sensor;
        };
        RobotMbedBehaviour.prototype.timerReset = function (port) {
            this.hardwareState.timers[port] = Date.now();
            U.debug('timerReset for ' + port);
        };
        RobotMbedBehaviour.prototype.timerGet = function (port) {
            var now = Date.now();
            var startTime = this.hardwareState.timers[port];
            if (startTime === undefined) {
                startTime = this.hardwareState.timers['start'];
            }
            var delta = now - startTime;
            U.debug('timerGet for ' + port + ' returned ' + delta);
            return delta;
        };
        RobotMbedBehaviour.prototype.ledOnAction = function (name, port, color) {
            var robotText = 'robot: ' + name + ', port: ' + port;
            U.debug(robotText + ' led on color ' + color);
            this.hardwareState.actions.led = {};
            this.hardwareState.actions.led.color = color;
        };
        RobotMbedBehaviour.prototype.statusLightOffAction = function (name, port) {
            var robotText = 'robot: ' + name + ', port: ' + port;
            U.debug(robotText + ' led off');
            this.hardwareState.actions.led = {};
            this.hardwareState.actions.led.mode = C.OFF;
        };
        RobotMbedBehaviour.prototype.toneAction = function (name, frequency, duration) {
            U.debug(name + ' piezo: ' + ', frequency: ' + frequency + ', duration: ' + duration);
            this.hardwareState.actions.tone = {};
            this.hardwareState.actions.tone.frequency = frequency;
            this.hardwareState.actions.tone.duration = duration;
            return duration;
        };
        RobotMbedBehaviour.prototype.motorOnAction = function (name, port, duration, speed) {
            var robotText = 'robot: ' + name + ', port: ' + port;
            var durText = duration === -1 ? ' w.o. duration' : (' for ' + duration + ' msec');
            U.debug(robotText + ' motor speed ' + speed + durText);
            if (this.hardwareState.actions.motors == undefined) {
                this.hardwareState.actions.motors = {};
            }
            if (port == "ab") {
                this.hardwareState.actions.motors.a = speed;
                this.hardwareState.actions.motors.b = speed;
            }
            else {
                this.hardwareState.actions.motors[port] = speed;
            }
            return 0;
        };
        RobotMbedBehaviour.prototype.motorStopAction = function (name, port) {
            var robotText = 'robot: ' + name + ', port: ' + port;
            U.debug(robotText + ' motor stop');
            this.motorOnAction(name, port, -1, 0);
        };
        RobotMbedBehaviour.prototype.showTextAction = function (text, mode) {
            var showText = "" + text;
            U.debug('***** show "' + showText + '" *****');
            var textLen = showText.length;
            var duration = 0;
            if (mode == C.TEXT) {
                duration = (textLen + 1) * 7 * 150;
            }
            else if (mode == C.CHARACTER && textLen > 1) {
                duration = textLen * 400;
            }
            this.hardwareState.actions.display = {};
            this.hardwareState.actions.display[mode.toLowerCase()] = showText;
            return duration;
        };
        RobotMbedBehaviour.prototype.showImageAction = function (image, mode) {
            var showImage = "" + image;
            U.debug('***** show "' + showImage + '" *****');
            var imageLen = image.length;
            var duration = 0;
            if (mode == C.ANIMATION) {
                duration = imageLen * 200;
            }
            this.hardwareState.actions.display = {};
            this.hardwareState.actions.display.picture = image;
            this.hardwareState.actions.display.mode = mode.toLowerCase();
            return duration;
        };
        RobotMbedBehaviour.prototype.displaySetBrightnessAction = function (value) {
            U.debug('***** set brightness "' + value + '" *****');
            this.hardwareState.actions.display = {};
            this.hardwareState.actions.display[C.BRIGHTNESS] = value;
            return 0;
        };
        RobotMbedBehaviour.prototype.displaySetPixelBrightnessAction = function (x, y, brightness) {
            U.debug('***** set pixel x="' + x + ", y=" + y + ", brightness=" + brightness + '" *****');
            this.hardwareState.actions.display = {};
            this.hardwareState.actions.display[C.PIXEL] = {};
            this.hardwareState.actions.display[C.PIXEL][C.X] = x;
            this.hardwareState.actions.display[C.PIXEL][C.Y] = y;
            this.hardwareState.actions.display[C.PIXEL][C.BRIGHTNESS] = brightness;
            return 0;
        };
        RobotMbedBehaviour.prototype.displayGetPixelBrightnessAction = function (s, x, y) {
            U.debug('***** get pixel x="' + x + ", y=" + y + '" *****');
            var sensor = this.hardwareState.sensors['display'][C.PIXEL];
            s.push(sensor[y][x]);
        };
        RobotMbedBehaviour.prototype.clearDisplay = function () {
            U.debug('clear display');
            this.hardwareState.actions.display = {};
            this.hardwareState.actions.display.clear = true;
            return 0;
        };
        RobotMbedBehaviour.prototype.writePinAction = function (pin, mode, value) {
            this.hardwareState.actions["pin" + pin] = {};
            this.hardwareState.actions["pin" + pin][mode] = {};
            this.hardwareState.actions["pin" + pin][mode] = value;
        };
        RobotMbedBehaviour.prototype.getState = function () {
            return this.hardwareState;
        };
        RobotMbedBehaviour.prototype.close = function () {
        };
        return RobotMbedBehaviour;
    }(interpreter_aRobotBehaviour_1.ARobotBehaviour));
    exports.RobotMbedBehaviour = RobotMbedBehaviour;
});
