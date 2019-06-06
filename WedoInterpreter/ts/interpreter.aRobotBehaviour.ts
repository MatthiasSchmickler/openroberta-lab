import { State } from "interpreter.state";

export abstract class ARobotBehaviour {

    abstract clearDisplay(): void;
    abstract getSample(s: State, name: string, port: number, sensor: string, slot: string): void;
    abstract timerReset(port: number): void;
    abstract timerGet(port: number): number;
    abstract ledOnAction(name: string, port: number, color: number): void;
    abstract statusLightOffAction(name: string, port: number): void;
    abstract toneAction(name: string, frequency: number, duration: number): void;
    abstract motorOnAction(name: string, port: number, duration: number, speed: number): void;
    abstract motorStopAction(name: string, port: number): void;
    abstract showTextAction(text: any): number;
    abstract close(): void;
}