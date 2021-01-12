package com.alipay.sofa.registry.server.meta.slot.tasks.reassign;

/**
 * @author chen.zhu
 * <p>
 * Jan 12, 2021
 */
public enum SlotAssignerState {
    Init {
        @Override
        void doAction(SlotAssigner assigner) {
            assigner.initialize();
        }

        @Override
        SlotAssignerState nextStep() {
            return Collect_Dead_Server_Slots;
        }
    },
    Collect_Dead_Server_Slots {
        @Override
        void doAction(SlotAssigner assigner) {
            assigner.getComparator().acceptRemoved(assigner);
        }

        @Override
        SlotAssignerState nextStep() {
            return Leave_Enough_Slots_For_New_DataServers;
        }
    },
    Leave_Enough_Slots_For_New_DataServers {
        @Override
        void doAction(SlotAssigner assigner) {
            assigner.getComparator().acceptRemains(assigner);
        }

        @Override
        SlotAssignerState nextStep() {
            return Assign_Slot_To_New_DataServers;
        }
    },
    Assign_Slot_To_New_DataServers {
        @Override
        void doAction(SlotAssigner assigner) {
            assigner.getComparator().acceptAdded(assigner);
        }

        @Override
        SlotAssignerState nextStep() {
            return Assign_Left_Slots_To_Remaining_DataServers;
        }
    },
    Assign_Left_Slots_To_Remaining_DataServers {
        @Override
        void doAction(SlotAssigner assigner) {
            for(String dataNode : assigner.getCurrentDataServers()) {
                assigner.assign(dataNode);
            }
        }

        @Override
        SlotAssignerState nextStep() {
            return End;
        }
    },
    End{
        @Override
        void doAction(SlotAssigner assigner) {
            //do nothing
        }

        @Override
        SlotAssignerState nextStep() {
            return Init;
        }
    };

    abstract void doAction(SlotAssigner assigner);

    abstract SlotAssignerState nextStep();
}
