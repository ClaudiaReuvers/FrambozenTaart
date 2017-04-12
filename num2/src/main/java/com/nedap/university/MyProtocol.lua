-- Created by IntelliJ IDEA.
-- User: claudia.reuvers
-- Date: 12/04/2017
-- Time: 14:02
-- To change this template use File | Settings | File Templates.
-- trivial protocol example
-- declare our protocol
trivial_proto = Proto("MyProtocol","Protocol for reliable UDP")
-- create a function to dissect it
function trivial_proto.dissector(buffer,pinfo,tree)
    pinfo.cols.protocol = "NEDAP"
    local subtree = tree:add(trivial_proto,buffer(),"Reliable UDP")
  --  subtree:add(buffer(0,2),"SourcePort: " .. buffer(0,2):uint())
   -- subtree = subtree:add(buffer(2,2),"The next two bytes") Nested subtree
    subtree:add(buffer(0,1),"Flags: " .. buffer(0,1):uint())
    subtree:add(buffer(1,4),"SeqNo: " .. buffer(1,4):uint())
    subtree:add(buffer(5,4),"AckNo: " .. buffer(5,4):uint())
    subtree:add(buffer(9,1),"Length: " .. buffer(9,1):uint())
    --subtree:add(buffer(9,2),"ackNo: " .. buffer(9,2):uint())
    --subtree:add(buffer(11,2),"checksum: " .. buffer(11,2):uint())


end
-- load the udp.port table
udp_table = DissectorTable.get("udp.port")
-- register our protocol to handle udp port 7777
udp_table:add(9876,trivial_proto)
