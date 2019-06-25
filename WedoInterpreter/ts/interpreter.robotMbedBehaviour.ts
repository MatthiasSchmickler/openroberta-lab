import { ARobotBehaviour } from "interpreter.aRobotBehaviour";
import { State } from "interpreter.state";
import * as C from "interpreter.constants";
import * as U from "interpreter.util";

export class RobotMbedBehaviour extends ARobotBehaviour {

    constructor() {
        super();

        U.loggingEnabled( false, false );
    }


    public getSample( s: State, name: string, sensor: string, port: any, slot: string ) {
        var robotText = 'robot: ' + name + ', port: ' + port;
        U.debug( robotText + ' getsample from ' + sensor );
        var sensorName = sensor;

        if ( sensorName == C.TIMER ) {
            s.push( this.timerGet( port ) );
        } else {
            s.push( this.getSensorValue( sensorName, port ) );
        }

    }

    private getSensorValue( sensorName: string, mode: string ): any {
        const sensor = this.hardwareState.sensors[sensorName];
        if ( sensor === undefined ) {
            return "undefined";
        }
        if ( mode != undefined ) {
            const v = sensor[mode];
            if ( v === undefined ) {
                return "undefined";
            }
            else {
                return v;
            }
        }
        return sensor;
    }

    public timerReset( port: number ) {
        this.hardwareState.timers[port] = Date.now();
        U.debug( 'timerReset for ' + port );
    }

    public timerGet( port: number ) {
        const now = Date.now();
        var startTime = this.hardwareState.timers[port];
        if ( startTime === undefined ) {
            startTime = this.hardwareState.timers['start'];
        }
        const delta = now - startTime;
        U.debug( 'timerGet for ' + port + ' returned ' + delta );
        return delta;
    }

    public ledOnAction( name: string, port: number, color: number ) {
        const robotText = 'robot: ' + name + ', port: ' + port;
        U.debug( robotText + ' led on color ' + color );
        this.hardwareState.actions.led = {};
        this.hardwareState.actions.led.color = color;
    }

    public statusLightOffAction( name: string, port: number ) {
        const robotText = 'robot: ' + name + ', port: ' + port;
        U.debug( robotText + ' led off' );
        this.hardwareState.actions.led = {};
        this.hardwareState.actions.led.mode = C.OFF;

    }

    public toneAction( name: string, frequency: number, duration: number ): number {
        U.debug( name + ' piezo: ' + ', frequency: ' + frequency + ', duration: ' + duration );
        this.hardwareState.actions.tone = {};
        this.hardwareState.actions.tone.frequency = frequency;
        this.hardwareState.actions.tone.duration = duration;
        return duration;
    }

    public motorOnAction( name: string, port: any, duration: number, speed: number ): number {
        const robotText = 'robot: ' + name + ', port: ' + port;
        const durText = duration === -1 ? ' w.o. duration' : ( ' for ' + duration + ' msec' );
        U.debug( robotText + ' motor speed ' + speed + durText );
        if ( this.hardwareState.actions.motors == undefined ) {
            this.hardwareState.actions.motors = {};
        }
        if ( port == "ab" ) {
            this.hardwareState.actions.motors.a = speed;
            this.hardwareState.actions.motors.b = speed;
        } else {
            this.hardwareState.actions.motors[port] = speed;
        }
        return 0;
    }

    public motorStopAction( name: string, port: any ) {
        const robotText = 'robot: ' + name + ', port: ' + port;
        U.debug( robotText + ' motor stop' );
        this.motorOnAction( name, port, -1, 0 );

    }

    public showTextAction( text: any, mode: string ): number {
        const showText = "" + text;
        U.debug( '***** show "' + showText + '" *****' );
        const textLen = showText.length;
        let duration = 0;
        if ( mode == C.TEXT ) {
            duration = ( textLen + 1 ) * 7 * 150;
        } else if ( mode == C.CHARACTER && textLen > 1 ) {
            duration = textLen * 400;
        }
        this.hardwareState.actions.display = {};
        this.hardwareState.actions.display[mode.toLowerCase()] = showText;
        return duration;
    }

    public showImageAction( image: any, mode: string ): number {
        const showImage = "" + image;
        U.debug( '***** show "' + showImage + '" *****' );
        const imageLen = image.length;
        let duration = 0;
        if ( mode == C.ANIMATION ) {
            duration = imageLen * 200;
        }
        this.hardwareState.actions.display = {};
        this.hardwareState.actions.display.picture = image;
        this.hardwareState.actions.display.mode = mode.toLowerCase();
        return duration;
    }

    public displaySetBrightnessAction( value: number ): number {
        U.debug( '***** set brightness "' + value + '" *****' );
        this.hardwareState.actions.display = {};
        this.hardwareState.actions.display[C.BRIGHTNESS] = value;
        return 0;
    }

    public displaySetPixelBrightnessAction( x: number, y: number, brightness: number ): number {
        U.debug( '***** set pixel x="' + x + ", y=" + y + ", brightness=" + brightness + '" *****' );
        this.hardwareState.actions.display = {};
        this.hardwareState.actions.display[C.PIXEL] = {};
        this.hardwareState.actions.display[C.PIXEL][C.X] = x;
        this.hardwareState.actions.display[C.PIXEL][C.Y] = y;
        this.hardwareState.actions.display[C.PIXEL][C.BRIGHTNESS] = brightness;
        return 0;
    }

    public displayGetPixelBrightnessAction( s: State, x: number, y: number ): void {
        U.debug( '***** get pixel x="' + x + ", y=" + y + '" *****' );
        const sensor = this.hardwareState.sensors[C.DISPLAY][C.PIXEL];
        s.push( sensor[y][x] );
    }


    public clearDisplay(): number {
        U.debug( 'clear display' );
        this.hardwareState.actions.display = {};
        this.hardwareState.actions.display.clear = true;
        return 0;
    }

    public writePinAction( pin: any, mode: string, value: number ): void {
        this.hardwareState.actions["pin" + pin] = {};
        this.hardwareState.actions["pin" + pin][mode] = {};
        this.hardwareState.actions["pin" + pin][mode] = value;
    }


    public getState(): any {
        return this.hardwareState;
    }

    public close() {
    }
}
