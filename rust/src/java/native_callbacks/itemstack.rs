use pumpkin_data::data_component_impl::EquipmentSlot;
use pumpkin_data::item::Item;
use pumpkin_data::item_stack::ItemStack as PumpkinItemStack;
use tokio::task::block_in_place;

use crate::{
    java::native_callbacks::{CALLBACK_CONTEXT, utils::with_player},
    proto::patchbukkit::{
        common::Uuid,
        itemstack::{
            GetPlayerInventoryResponse, ItemStack as ProtoItemStack, SetPlayerEquipmentRequest,
            SetPlayerInventorySlotRequest, SetPlayerSelectedSlotRequest,
        },
    },
};

fn pumpkin_item_to_proto(stack: &PumpkinItemStack) -> ProtoItemStack {
    if stack.is_empty() {
        ProtoItemStack {
            r#type: "minecraft:air".to_string(),
            amount: 0,
        }
    } else {
        let registry_key = stack.item.registry_key;
        let r#type = if registry_key.starts_with("minecraft:") {
            registry_key.to_string()
        } else {
            format!("minecraft:{registry_key}")
        };
        ProtoItemStack {
            r#type,
            amount: u32::from(stack.item_count),
        }
    }
}

fn proto_item_to_pumpkin(proto: Option<&ProtoItemStack>) -> PumpkinItemStack {
    let Some(proto) = proto else {
        return PumpkinItemStack::EMPTY.clone();
    };
    if proto.amount == 0 || proto.r#type.is_empty() {
        return PumpkinItemStack::EMPTY.clone();
    }
    let key = proto
        .r#type
        .strip_prefix("minecraft:")
        .unwrap_or(&proto.r#type);
    if let Some(item) = Item::from_registry_key(key) {
        PumpkinItemStack::new(proto.amount as u8, item)
    } else {
        PumpkinItemStack::EMPTY.clone()
    }
}

pub fn ffi_native_bridge_get_player_inventory_impl(
    request: Uuid,
) -> Option<GetPlayerInventoryResponse> {
    let ctx = CALLBACK_CONTEXT.get()?;
    with_player(Some(&request), |player| {
        let player = player.clone();
        block_in_place(|| {
            ctx.runtime.block_on(async {
                let selected_slot = u32::from(player.inventory.get_selected_slot());

                let main_guard = player.inventory.main_inventory.read().await;
                let main_inventory = main_guard.iter().map(pumpkin_item_to_proto).collect();

                let eq_guard = player.inventory.entity_equipment.lock().await;
                let off_hand = pumpkin_item_to_proto(&eq_guard.get(&EquipmentSlot::OFF_HAND));
                let helmet = pumpkin_item_to_proto(&eq_guard.get(&EquipmentSlot::HEAD));
                let chestplate = pumpkin_item_to_proto(&eq_guard.get(&EquipmentSlot::CHEST));
                let leggings = pumpkin_item_to_proto(&eq_guard.get(&EquipmentSlot::LEGS));
                let boots = pumpkin_item_to_proto(&eq_guard.get(&EquipmentSlot::FEET));

                GetPlayerInventoryResponse {
                    main_inventory,
                    selected_slot,
                    off_hand: Some(off_hand),
                    helmet: Some(helmet),
                    chestplate: Some(chestplate),
                    leggings: Some(leggings),
                    boots: Some(boots),
                }
            })
        })
    })
}

pub fn ffi_native_bridge_set_player_inventory_slot_impl(
    request: SetPlayerInventorySlotRequest,
) -> Option<()> {
    let ctx = CALLBACK_CONTEXT.get()?;
    let slot = request.slot as usize;
    let pumpkin_item = proto_item_to_pumpkin(request.item.as_ref());

    with_player(request.uuid.as_ref(), |player| {
        let player = player.clone();
        ctx.runtime.spawn(async move {
            if slot < 36 {
                let mut main_guard = player.inventory.main_inventory.write().await;
                main_guard[slot] = pumpkin_item;
            } else {
                let eq_slot = match slot {
                    36 => Some(EquipmentSlot::FEET),
                    37 => Some(EquipmentSlot::LEGS),
                    38 => Some(EquipmentSlot::CHEST),
                    39 => Some(EquipmentSlot::HEAD),
                    40 => Some(EquipmentSlot::OFF_HAND),
                    _ => None,
                };
                if let Some(eq_slot) = eq_slot {
                    let mut eq_guard = player.inventory.entity_equipment.lock().await;
                    eq_guard.put(&eq_slot, pumpkin_item);
                }
            }
        });
    })
}

pub fn ffi_native_bridge_set_player_selected_slot_impl(
    request: SetPlayerSelectedSlotRequest,
) -> Option<()> {
    with_player(request.uuid.as_ref(), |player| {
        if request.slot < 9 {
            player.inventory.set_selected_slot(request.slot as u8);
        }
    })
}

pub fn ffi_native_bridge_set_player_equipment_impl(
    request: SetPlayerEquipmentRequest,
) -> Option<()> {
    let ctx = CALLBACK_CONTEXT.get()?;
    let pumpkin_item = proto_item_to_pumpkin(request.item.as_ref());
    let slot_type = request.slot_type;

    with_player(request.uuid.as_ref(), |player| {
        let player = player.clone();
        ctx.runtime.spawn(async move {
            match slot_type {
                0 => {
                    // Main Hand
                    player.inventory.set_held_item(pumpkin_item).await;
                }
                1 => {
                    // Off Hand
                    let mut eq = player.inventory.entity_equipment.lock().await;
                    eq.put(&EquipmentSlot::OFF_HAND, pumpkin_item);
                }
                2 => {
                    // Feet
                    let mut eq = player.inventory.entity_equipment.lock().await;
                    eq.put(&EquipmentSlot::FEET, pumpkin_item);
                }
                3 => {
                    // Legs
                    let mut eq = player.inventory.entity_equipment.lock().await;
                    eq.put(&EquipmentSlot::LEGS, pumpkin_item);
                }
                4 => {
                    // Chest
                    let mut eq = player.inventory.entity_equipment.lock().await;
                    eq.put(&EquipmentSlot::CHEST, pumpkin_item);
                }
                5 => {
                    // Head
                    let mut eq = player.inventory.entity_equipment.lock().await;
                    eq.put(&EquipmentSlot::HEAD, pumpkin_item);
                }
                _ => {}
            }
        });
    })
}

pub fn ffi_native_bridge_clear_player_inventory_impl(request: Uuid) -> Option<()> {
    let ctx = CALLBACK_CONTEXT.get()?;
    with_player(Some(&request), |player| {
        let player = player.clone();
        ctx.runtime.spawn(async move {
            let mut main_guard = player.inventory.main_inventory.write().await;
            main_guard.fill_with(|| PumpkinItemStack::EMPTY.clone());
            let mut eq_guard = player.inventory.entity_equipment.lock().await;
            eq_guard.clear();
        });
    })
}
