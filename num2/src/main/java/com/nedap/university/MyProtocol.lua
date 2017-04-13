-- Created by IntelliJ IDEA.
-- User: claudia.reuvers
-- Date: 12/04/2017
-- Time: 14:02
-- To change this template use File | Settings | File Templates.
-- trivial protocol example
-- declare our protocol

trivial_proto = Proto("MyProtocol","Protocol for reliable UDP")
local f_whole = ProtoField.uint8("myproto.value.hex", "Flags", base.HEX)
local f_part1 = ProtoField.uint8("myproto.syn", "SYN ACK FIN PAUSE", base.DEC, nil, 0xF)
local f_part2 = ProtoField.uint8("myproto.syn", "unused", base.DEC, nil, 0xF0)

trivial_proto.fields = {f_value, f_whole, f_part1, f_part2}
-- create a function to dissect it
function trivial_proto.dissector(buffer,pinfo,tree)

    pinfo.cols.protocol = "NEDAP"
    local subtree = tree:add(trivial_proto,buffer(),"Reliable UDP")
   subtree:add(buffer(9,1),"Length: " .. buffer(9,1):uint())
   subtree:add(buffer(1,4),"AckNo: " .. buffer(1,4):uint())
   subtree:add(buffer(5,4),"SeqNo: " .. buffer(5,4):uint())
   local subsubtree = subtree:add(f_whole, buffer:range(0,1))
   subsubtree:add(f_part2, buffer:range(0,1))
   subsubtree:add(f_part1, buffer:range(0,1))

end
-- load the udp.port table
udp_table = DissectorTable.get("udp.port")
-- register our protocol to handle udp port 7777
udp_table:add(9876,trivial_proto)