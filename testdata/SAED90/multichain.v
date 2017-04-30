
module multichain (reset,
clk000, clk001, 
test_si000, test_si001, 
test_so000, test_so001, 
test_en, 
in000_00, in000_01, in000_02,
in001_00, in001_01,
out000_00, out000_01, out000_02,
out001_00, out001_01
  );
input  reset ;
input  clk000, clk001 ;
input test_si000, test_si001;
output test_so000, test_so001; 
input test_en;
input in000_00, in000_01, in000_02;
input in001_00, in001_01;
output out000_00, out000_01, out000_02;
output out001_00, out001_01;

INVX2 rstinv (.IN ( reset ) , .QN ( rstb ) ) ;
SDFFARX2 reg000_00 (.QN(none00), .Q(out000_00), .CLK(clk000), .RSTB(rstb), .SE(test_en), .SI(test_si000), .D(in000_00));
SDFFARX2 reg000_01 (.QN(none01), .Q(out000_01), .CLK(clk000), .RSTB(rstb), .SE(test_en), .SI(out000_00), .D(in000_01));
SDFFARX2 reg000_02 (.QN(none02), .Q(out000_02), .CLK(clk000), .RSTB(rstb), .SE(test_en), .SI(out000_01), .D(in000_02));
NBUFFX2 testso1 (.IN ( out000_02 ) , .Q ( test_so000 ) ) ;
SDFFARX2 reg001_00 (.QN(none10), .Q(out001_00), .CLK(clk001), .RSTB(rstb), .SE(test_en), .SI(test_si001), .D(in001_00));
SDFFARX2 reg001_01 (.QN(none11), .Q(out001_01), .CLK(clk001), .RSTB(rstb), .SE(test_en), .SI(out001_00), .D(in001_01));
NBUFFX2 testso2 (.IN ( out001_01 ) , .Q ( test_so001 ) ) ;
endmodule


