import { ARobotBehaviour } from "interpreter.aRobotBehaviour";
import { State } from "interpreter.state";
import * as C from "interpreter.constants";
import * as U from "interpreter.util";

export class RobotMbedBehaviour extends ARobotBehaviour {

    constructor() {
        super();

        U.loggingEnabled(false, false);
    }

    public clearDisplay() {
        U.debug('clear display');
    }

    public getSample(s: State, name: string, port: number, sensor: string, slot: string) {
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
    }

    public timerReset(port: number) {
        //        this.timers[port] = Date.now();
        U.debug('timerReset for ' + port);
    }

    public timerGet(port: number) {
        const now = Date.now();
        var startTime = this.hardwareState.timers[port];
        if (startTime === undefined) {
            startTime = this.hardwareState.timers['start'];
        }
        const delta = now - startTime;
        U.debug('timerGet for ' + port + ' returned ' + delta);
        return delta;
    }

    public ledOnAction(name: string, port: number, color: number) {
        const robotText = 'robot: ' + name + ', port: ' + port;
        U.debug(robotText + ' led on color ' + color);
        this.hardwareState.actions.led = {};
        this.hardwareState.actions.led.color = color;
    }

    public statusLightOffAction(name: string, port: number) {
        //        var brickid = WEDO.getBrickIdByName( name );
        const robotText = 'robot: ' + name + ', port: ' + port;
        U.debug(robotText + ' led off');
        //        const cmd = { 'target': 'wedo', 'type': 'command', 'actuator': 'light', 'brickid': brickid, 'color': 0 };
        //        WEBVIEW_C.jsToAppInterface( cmd );
    }

    public toneAction(name: string, frequency: number, duration: number) {
        //        var brickid = WEDO.getBrickIdByName( name ); // TODO: better style
        const robotText = 'robot: ' + name;
        U.debug(robotText + ' piezo: ' + ', frequency: ' + frequency + ', duration: ' + duration);
        //        const cmd = { 'target': 'wedo', 'type': 'command', 'actuator': 'piezo', 'brickid': brickid, 'frequency': frequency, 'duration': duration };
        //        WEBVIEW_C.jsToAppInterface( cmd );
    }

    public motorOnAction(name: string, port: number, duration: number, speed: number) {
        //        var brickid = WEDO.getBrickIdByName( name ); // TODO: better style
        const robotText = 'robot: ' + name + ', port: ' + port;
        const durText = duration === -1 ? ' w.o. duration' : (' for ' + duration + ' msec');
        U.debug(robotText + ' motor speed ' + speed + durText);
        //        const cmd = { 'target': 'wedo', 'type': 'command', 'actuator': 'motor', 'brickid': brickid, 'action': 'on', 'id': port, 'direction': speed < 0 ? 1 : 0, 'power': Math.abs( speed ) };
        //        WEBVIEW_C.jsToAppInterface( cmd );
    }

    public motorStopAction(name: string, port: number) {
        //        var brickid = WEDO.getBrickIdByName( name ); // TODO: better style
        const robotText = 'robot: ' + name + ', port: ' + port;
        U.debug(robotText + ' motor stop');
        //        const cmd = { 'target': 'wedo', 'type': 'command', 'actuator': 'motor', 'brickid': brickid, 'action': 'stop', 'id': port };
        //        WEBVIEW_C.jsToAppInterface( cmd );
    }

    public showTextAction(text: any, mode: string): number {
        const showText = "" + text;
        U.debug('***** show "' + showText + '" *****');
        const textLen = showText.length;
        let duration = 0;
        if (mode == C.TEXT) {
            duration = (textLen + 1) * 7 * 150;
        } else if (mode == C.CHARACTER && textLen > 1) {
            duration = textLen * 400;
        }
        this.hardwareState.actions.display = {};
        this.hardwareState.actions.display[mode.toLowerCase()] = showText;
        return duration;
    }

    public showImageAction(image: any, mode: string): number {
        const showImage = "" + image;
        U.debug('***** show "' + showImage + '" *****');
        const imageLen = image.length;
        let duration = 0;
        if (mode == C.ANIMATION) {
            duration = imageLen * 200;
        }
        this.hardwareState.actions.display = {};
        this.hardwareState.actions.display.picture = image;
        this.hardwareState.actions.display.mode = mode.toLowerCase();
        return duration;
    }

    public getState(): any {
        return this.hardwareState;
    }

    public close() {
    }
}
