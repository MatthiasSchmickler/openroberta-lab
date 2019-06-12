define(["require", "exports"], function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var ARobotBehaviour = (function () {
        function ARobotBehaviour() {
            this.hardwareState = {};
            this.hardwareState.timers = {};
            this.hardwareState.actions = {};
            this.hardwareState.sensors = {};
        }
        ARobotBehaviour.prototype.getActionState = function (actionType, mode, resetState) {
            if (resetState === void 0) { resetState = false; }
            var v = this.hardwareState.actions[actionType][mode];
            if (resetState) {
                delete this.hardwareState.actions[actionType][mode];
            }
            return v;
        };
        ARobotBehaviour.prototype.updateSensorState = function (state) {
            this.hardwareState.sensors = state;
        };
        return ARobotBehaviour;
    }());
    exports.ARobotBehaviour = ARobotBehaviour;
});
