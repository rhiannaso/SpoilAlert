package edu.ucsb.cs.cs184.rhiannaso.spoilalert

import java.util.*

data class ItemCard(val item_name : String, var item_quantity : String, val item_expiration : Date, val eid : String, val nid : String, val owner : String, val owner_name : String) {
}