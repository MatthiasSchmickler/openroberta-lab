import { State } from "interpreter.state";

export abstract class ARobotBehaviour {
    protected hardwareState;

    constructor() {
        this.hardwareState = {};
        this.hardwareState.timers = {};
        this.hardwareState.timers['start'] = Date.now();
        this.hardwareState.actions = {};
        this.hardwareState.sensors = {};
    }

    public getActionState( actionType: string, mode: string, resetState = false ): any {
        let v = this.hardwareState.actions[actionType][mode];
        if ( resetState ) {
            delete this.hardwareState.actions[actionType][mode];
        }
        return v;
    }
    

    abstract clearDisplay(): void;
    abstract getSample( s: State, name: string, sensor: string, port: number, slot: string ): void;
    abstract timerReset( port: number ): void;
    abstract timerGet( port: number ): number;
    abstract ledOnAction( name: string, port: number, color: number ): void;
    abstract statusLightOffAction( name: string, port: number ): void;
    abstract toneAction( name: string, frequency: number, duration: number ): void;
    abstract motorOnAction( name: string, port: any, duration: number, speed: number ): number;
    abstract motorStopAction( name: string, port: any ): void;
    abstract writePinAction( pin: any, mode: string, value: number ): void;
    abstract showTextAction( text: any, mode: string ): number;
    abstract showImageAction( image: any, mode: string ): number;
    abstract displaySetBrightnessAction( value: number ): number;
    abstract displaySetPixelAction( x: number, y: number, brightness: number ): number;

    abstract close(): void;
}