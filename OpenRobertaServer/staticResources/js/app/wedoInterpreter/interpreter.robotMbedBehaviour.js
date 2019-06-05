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
            _this.hardwareState = {};
            _this.hardwareState.timers = {};
            _this.hardwareState.actions = {};
            _this.hardwareState.sensors = {};
            U.loggingEnabled(false, false);
            return _this;
        }
        RobotMbedBehaviour.prototype.clearDisplay = function () {
            U.debug('clear display');
        };
        RobotMbedBehaviour.prototype.getSample = function (s, name, port, sensor, slot) {
            var robotText = 'robot: ' + name + ', port: ' + port;
            U.debug(robotText + ' getsample from ' + sensor);
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
            //        s.push( WEDO.getSensorValue( brickid, sensorName, port, slot ) );
        };
        RobotMbedBehaviour.prototype.timerReset = function (port) {
            //        this.timers[port] = Date.now();
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
            //        var brickid = WEDO.getBrickIdByName( name );
            var robotText = 'robot: ' + name + ', port: ' + port;
            U.debug(robotText + ' led on color ' + color);
            this.hardwareState.actions.led = {};
            this.hardwareState.actions.led.color = color;
        };
        RobotMbedBehaviour.prototype.statusLightOffAction = function (name, port) {
            //        var brickid = WEDO.getBrickIdByName( name );
            var robotText = 'robot: ' + name + ', port: ' + port;
            U.debug(robotText + ' led off');
            //        const cmd = { 'target': 'wedo', 'type': 'command', 'actuator': 'light', 'brickid': brickid, 'color': 0 };
            //        WEBVIEW_C.jsToAppInterface( cmd );
        };
        RobotMbedBehaviour.prototype.toneAction = function (name, frequency, duration) {
            //        var brickid = WEDO.getBrickIdByName( name ); // TODO: better style
            var robotText = 'robot: ' + name;
            U.debug(robotText + ' piezo: ' + ', frequency: ' + frequency + ', duration: ' + duration);
            //        const cmd = { 'target': 'wedo', 'type': 'command', 'actuator': 'piezo', 'brickid': brickid, 'frequency': frequency, 'duration': duration };
            //        WEBVIEW_C.jsToAppInterface( cmd );
        };
        RobotMbedBehaviour.prototype.motorOnAction = function (name, port, duration, speed) {
            //        var brickid = WEDO.getBrickIdByName( name ); // TODO: better style
            var robotText = 'robot: ' + name + ', port: ' + port;
            var durText = duration === -1 ? ' w.o. duration' : (' for ' + duration + ' msec');
            U.debug(robotText + ' motor speed ' + speed + durText);
            //        const cmd = { 'target': 'wedo', 'type': 'command', 'actuator': 'motor', 'brickid': brickid, 'action': 'on', 'id': port, 'direction': speed < 0 ? 1 : 0, 'power': Math.abs( speed ) };
            //        WEBVIEW_C.jsToAppInterface( cmd );
        };
        RobotMbedBehaviour.prototype.motorStopAction = function (name, port) {
            //        var brickid = WEDO.getBrickIdByName( name ); // TODO: better style
            var robotText = 'robot: ' + name + ', port: ' + port;
            U.debug(robotText + ' motor stop');
            //        const cmd = { 'target': 'wedo', 'type': 'command', 'actuator': 'motor', 'brickid': brickid, 'action': 'stop', 'id': port };
            //        WEBVIEW_C.jsToAppInterface( cmd );
        };
        RobotMbedBehaviour.prototype.showTextAction = function (text) {
            var showText = "" + text;
            U.debug('***** show "' + showText + '" *****');
            var duration = (showText.length + 1) * 7 * 150;
            this.hardwareState.actions.display = {};
            this.hardwareState.actions.display.text = showText;
            this.hardwareState.delayInterpreter = duration;
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
