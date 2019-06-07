import { ARobotBehaviour } from "interpreter.aRobotBehaviour";
import { State } from "interpreter.state";
import * as C from "interpreter.constants";
import * as U from "interpreter.util";

export class RobotWeDoBehaviour extends ARobotBehaviour {
    /*
     * represents the state of connected wedo devices with the following
     * structure: {<name of the device> { 1 : { tiltsensor : "0.0" }, 2 : {
     * motionsensor : "4.0 }, batterylevel : "100", button : "false" }
     */
    private btInterfaceFct;
    private toDisplayFct;
    private timers;
    private wedo = {};
    private tiltMode = {
        UP: '3.0',
        DOWN: '9.0',
        BACK: '5.0',
        FRONT: '7.0',
        NO: '0.0'
    }

    constructor( btInterfaceFct: any, toDisplayFct: any ) {
        super();
        this.btInterfaceFct = btInterfaceFct;
        this.toDisplayFct = toDisplayFct;
        this.timers = {};
        this.timers['start'] = Date.now();

        U.loggingEnabled( false, false );
    }


    public update( data ) {
        if ( data.target !== "wedo" ) {
            return;
        }
        switch ( data.type ) {
            case "connect":
                if ( data.state == "connected" ) {
                    this.wedo[data.brickid] = {};
                    this.wedo[data.brickid]["brickname"] = data.brickname.replace( /\s/g, '' ).toUpperCase();
                    // for some reason we do not get the inital state of the button, so here it is hardcoded
                    this.wedo[data.brickid]["button"] = 'false';
                } else if ( data.state == "disconnected" ) {
                    delete this.wedo[data.brickid];
                }
                break;
            case "didAddService":
                if ( data.state == "connected" ) {
                    if ( data.id && data.sensor ) {
                        this.wedo[data.brickid][data.id] = {};
                        this.wedo[data.brickid][data.id][data.sensor.replace( /\s/g, '' ).toLowerCase()] = '';
                    } else if ( data.id && data.actuator ) {
                        this.wedo[data.brickid][data.id] = {};
                        this.wedo[data.brickid][data.id][data.actuator.replace( /\s/g, '' ).toLowerCase()] = '';
                    } else if ( data.sensor ) {
                        this.wedo[data.brickid][data.sensor.replace( /\s/g, '' ).toLowerCase()] = '';
                    } else {
                        this.wedo[data.brickid][data.actuator.replace( /\s/g, '' ).toLowerCase()] = '';
                    }
                }
                break;
            case "didRemoveService":
                if ( data.id ) {
                    delete this.wedo[data.brickid][data.id];
                } else if ( data.sensor ) {
                    delete this.wedo[data.brickid][data.sensor.replace( /\s/g, '' ).toLowerCase()]
                } else {
                    delete this.wedo[data.brickid][data.actuator.replace( /\s/g, '' ).toLowerCase()]
                }
                break;
            case "update":
                if ( data.id ) {
                    this.wedo[data.brickid][data.id][data.sensor.replace( /\s/g, '' ).toLowerCase()] = data.state;
                } else {
                    this.wedo[data.brickid][data.sensor.replace( /\s/g, '' ).toLowerCase()] = data.state;
                }
                break;
            default:
                // TODO think about what could happen here.
                break;
        }
        U.info( this.wedo );
    }


    public getSensorValue( brickid, sensor, id, slot ) {
        switch ( sensor ) {
            case "tiltsensor":
                if ( slot === "ANY" ) {
                    return this.wedo[brickid][id][sensor] !== this.tiltMode.NO;
                } else {
                    return this.wedo[brickid][id][sensor] === this.tiltMode[slot];
                }
            case "motionsensor":
                return parseInt( this.wedo[brickid][id][sensor] );
            case "button":
                return this.wedo[brickid][sensor] === "true";
        }
    }

    public getConnectedBricks() {
        var brickids = [];
        for ( var brickid in this.wedo ) {
            if ( this.wedo.hasOwnProperty( brickid ) ) {
                brickids.push( brickid );
            }
        }
        return brickids;
    }


    public getBrickIdByName( name ) {
        for ( var brickid in this.wedo ) {
            if ( this.wedo.hasOwnProperty( brickid ) ) {
                if ( this.wedo[brickid].brickname === name.toUpperCase() ) {
                    return brickid;
                }
            }
        }
        return null;
    }

    public getBrickById( id ) {
        return this.wedo[id];
    }

    public clearDisplay() {
        U.debug( 'clear display' );
        this.toDisplayFct( { "clear": true } );
    }

    public getSample( s: State, name: string, port: number, sensor: string, slot: string ) {
        var robotText = 'robot: ' + name + ', port: ' + port;
        U.info( robotText + ' getsample from ' + sensor );
        var sensorName;
        switch ( sensor ) {
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
                s.push( this.timerGet( port ) );
                return;
            default:
                throw 'invalid get sample for ' + name + ' - ' + port + ' - ' + sensor + ' - ' + slot;
        }
        var brickid = this.getBrickIdByName( name );
        s.push( this.getSensorValue( brickid, sensorName, port, slot ) );
    }

    public timerReset( port: number ) {
        this.timers[port] = Date.now();
        U.debug( 'timerReset for ' + port );
    }

    public timerGet( port: number ) {
        const now = Date.now();
        var startTime = this.timers[port];
        if ( startTime === undefined ) {
            startTime = this.timers['start'];
        }
        const delta = now - startTime;
        U.debug( 'timerGet for ' + port + ' returned ' + delta );
        return delta;
    }

    public ledOnAction( name: string, port: number, color: number ) {
        var brickid = this.getBrickIdByName( name );
        const robotText = 'robot: ' + name + ', port: ' + port;
        U.debug( robotText + ' led on color ' + color );
        const cmd = { 'target': 'wedo', 'type': 'command', 'actuator': 'light', 'brickid': brickid, 'color': color };
        this.btInterfaceFct( cmd );
    }

    public statusLightOffAction( name: string, port: number ) {
        var brickid = this.getBrickIdByName( name );
        const robotText = 'robot: ' + name + ', port: ' + port;
        U.debug( robotText + ' led off' );
        const cmd = { 'target': 'wedo', 'type': 'command', 'actuator': 'light', 'brickid': brickid, 'color': 0 };
        this.btInterfaceFct( cmd );
    }

    public toneAction( name: string, frequency: number, duration: number ) {
        var brickid = this.getBrickIdByName( name ); // TODO: better style
        const robotText = 'robot: ' + name;
        U.debug( robotText + ' piezo: ' + ', frequency: ' + frequency + ', duration: ' + duration );
        const cmd = { 'target': 'wedo', 'type': 'command', 'actuator': 'piezo', 'brickid': brickid, 'frequency': frequency, 'duration': duration };
        this.btInterfaceFct( cmd );
    }

    public motorOnAction( name: string, port: number, duration: number, speed: number ) {
        var brickid = this.getBrickIdByName( name ); // TODO: better style
        const robotText = 'robot: ' + name + ', port: ' + port;
        const durText = duration === -1 ? ' w.o. duration' : ( ' for ' + duration + ' msec' );
        U.debug( robotText + ' motor speed ' + speed + durText );
        const cmd = { 'target': 'wedo', 'type': 'command', 'actuator': 'motor', 'brickid': brickid, 'action': 'on', 'id': port, 'direction': speed < 0 ? 1 : 0, 'power': Math.abs( speed ) };
        this.btInterfaceFct( cmd );
    }

    public motorStopAction( name: string, port: number ) {
        var brickid = this.getBrickIdByName( name ); // TODO: better style
        const robotText = 'robot: ' + name + ', port: ' + port;
        U.debug( robotText + ' motor stop' );
        const cmd = { 'target': 'wedo', 'type': 'command', 'actuator': 'motor', 'brickid': brickid, 'action': 'stop', 'id': port };
        this.btInterfaceFct( cmd );
    }

    public showTextAction( text: any ): number {
        const showText = "" + text;
        U.debug( '***** show "' + showText + '" *****' );
        this.toDisplayFct( { "show": showText } );
        return 0;
    }

    public showImageAction( _text: any, _mode: string ): number {
        U.debug( '***** show image not supported by WeDo *****' );
        return 0;
    }

    public close() {
        var ids = this.getConnectedBricks();
        for ( let id in ids ) {
            if ( ids.hasOwnProperty( id ) ) {
                var name = this.getBrickById( ids[id] ).brickname;
                this.motorStopAction( name, 1 );
                this.motorStopAction( name, 2 );
                this.ledOnAction( name, 99, 3 );
            }
        }
    }
}
