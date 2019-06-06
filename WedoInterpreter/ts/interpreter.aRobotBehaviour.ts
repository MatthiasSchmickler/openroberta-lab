import { State } from "interpreter.state";

export abstract class ARobotBehaviour {
    protected hardwareState;

    constructor() {
        this.hardwareState = {};
        this.hardwareState.timers = {};
        this.hardwareState.actions = {};
        this.hardwareState.sensors = {};
    }

    public getActionState(actionType: string, mode: string, resetState = false): any {
        let v = this.hardwareState.actions[actionType][mode];
        if (resetState) {
            delete this.hardwareState.actions[actionType][mode];
        }
        return v;
    }

    abstract clearDisplay(): void;
    abstract getSample(s: State, name: string, port: number, sensor: string, slot: string): void;
    abstract timerReset(port: number): void;
    abstract timerGet(port: number): number;
    abstract ledOnAction(name: string, port: number, color: number): void;
    abstract statusLightOffAction(name: string, port: number): void;
    abstract toneAction(name: string, frequency: number, duration: number): void;
    abstract motorOnAction(name: string, port: number, duration: number, speed: number): void;
    abstract motorStopAction(name: string, port: number): void;
    abstract showTextAction(text: any, mode: string): number;
    abstract showImageAction(image: any, mode: string): number;
    abstract close(): void;
}