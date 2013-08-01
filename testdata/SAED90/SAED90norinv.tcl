set SC_LIB saed90nm_typ
set LIB_PATH ../SAED_EDK90nm/Digital_Standard_cell_Library/synopsys

set target_library ${LIB_PATH}/models/${SC_LIB}.db
set link_library "* $target_library"
set symbol_library ${LIB_PATH}/icons/saed90nm.sdb

set_dont_use ${SC_LIB}/*
remove_attribute ${SC_LIB}/NOR2X1 dont_use
remove_attribute ${SC_LIB}/INVX1 dont_use

define_design_lib WORK -path "work"
analyze -library WORK -format verilog SAED90cells.v 
elaborate SAED90cells -architecture verilog -library WORK -update

check_design
compile_ultra
write -hierarchy -format verilog -output SAED90norinv.v
