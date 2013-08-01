
module b01 ( line1, line2, reset, outp, overflw, clock, Scan_Enable, Scan_In, 
        Scan_Out );
  input line1, line2, reset, clock, Scan_Enable, Scan_In;
  output outp, overflw, Scan_Out;
  wire   N44, N45, N47, n105, n144, n154, n178, n197, n213, n215, n216, n217,
         n218, n219, n220, n221, n222, n223, n224, n225, n226, n227, n228,
         n229, n230, n231, n232, n233, n234;
  wire   [2:0] stato;

  SDFFARX1 overflw_reg ( .D(n105), .SI(stato[2]), .SE(Scan_Enable), .CLK(clock), .RSTB(n197), .Q(overflw) );
  SDFFARX1 \stato_reg[0]  ( .D(N44), .SI(outp), .SE(Scan_Enable), .CLK(clock), 
        .RSTB(n197), .Q(stato[0]), .QN(n215) );
  SDFFARX1 \stato_reg[2]  ( .D(n144), .SI(n154), .SE(Scan_Enable), .CLK(clock), 
        .RSTB(n197), .Q(stato[2]), .QN(n213) );
  SDFFARX1 \stato_reg[1]  ( .D(N45), .SI(stato[0]), .SE(Scan_Enable), .CLK(
        clock), .RSTB(n197), .Q(n154), .QN(n178) );
  SDFFARX1 outp_reg ( .D(N47), .SI(Scan_In), .SE(Scan_Enable), .CLK(clock), 
        .RSTB(n197), .Q(outp) );
  NAND2X0 U100 ( .IN1(n226), .IN2(n225), .QN(n227) );
  NAND2X0 U101 ( .IN1(stato[2]), .IN2(n178), .QN(n231) );
  NAND2X0 U102 ( .IN1(n222), .IN2(n215), .QN(n233) );
  NAND2X0 U103 ( .IN1(n229), .IN2(n213), .QN(n234) );
  NAND2X0 U104 ( .IN1(n105), .IN2(n226), .QN(n219) );
  NAND2X0 U105 ( .IN1(n178), .IN2(n217), .QN(n220) );
  NAND2X0 U106 ( .IN1(n216), .IN2(n230), .QN(n217) );
  NAND2X0 U107 ( .IN1(stato[2]), .IN2(n225), .QN(n230) );
  NAND2X0 U108 ( .IN1(stato[0]), .IN2(n226), .QN(n221) );
  NAND2X0 U109 ( .IN1(n229), .IN2(n215), .QN(n218) );
  NAND2X0 U110 ( .IN1(line2), .IN2(line1), .QN(n226) );
  INVX0 U111 ( .IN(reset), .QN(n197) );
  NBUFFX2 U112 ( .IN(overflw), .Q(Scan_Out) );
  NOR3X0 U113 ( .IN1(stato[2]), .IN2(n178), .IN3(n215), .QN(n105) );
  INVX0 U114 ( .IN(n226), .QN(n229) );
  NAND3X0 U115 ( .IN1(n213), .IN2(n218), .IN3(n221), .QN(n216) );
  OR2X1 U116 ( .IN1(line2), .IN2(line1), .Q(n225) );
  OA22X1 U117 ( .IN1(n178), .IN2(n218), .IN3(n215), .IN4(n230), .Q(n224) );
  NAND3X0 U118 ( .IN1(n220), .IN2(n219), .IN3(n224), .QN(N44) );
  OA22X1 U119 ( .IN1(n225), .IN2(n231), .IN3(n154), .IN4(n221), .Q(n223) );
  NOR2X0 U120 ( .IN1(stato[2]), .IN2(n178), .QN(n222) );
  NAND3X0 U121 ( .IN1(n224), .IN2(n223), .IN3(n233), .QN(N45) );
  OA21X1 U122 ( .IN1(stato[0]), .IN2(n178), .IN3(stato[2]), .Q(n228) );
  XNOR2X1 U123 ( .IN1(n228), .IN2(n227), .Q(N47) );
  OA22X1 U124 ( .IN1(n215), .IN2(n231), .IN3(n230), .IN4(n154), .Q(n232) );
  NAND3X0 U125 ( .IN1(n234), .IN2(n233), .IN3(n232), .QN(n144) );
endmodule

