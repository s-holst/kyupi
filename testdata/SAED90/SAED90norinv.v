
module SAED90cells ( in_INP, in_IN1, in_IN2, in_IN3, in_IN4, in_IN5, in_IN6, 
        in_S, in_S0, in_S1, in_A, in_B, in_CI, in_A0, in_B0, out_AND2X1, 
        out_AND2X2, out_AND2X4, out_AND3X1, out_AND3X2, out_AND3X4, out_AND4X1, 
        out_AND4X2, out_AND4X4, out_AO21X1, out_AO21X2, out_AO221X1, 
        out_AO221X2, out_AO222X1, out_AO222X2, out_AO22X1, out_AO22X2, 
        out_AOBUFX1, out_AOBUFX2, out_AOBUFX4, out_AOI21X1, out_AOI21X2, 
        out_AOI221X1, out_AOI221X2, out_AOI222X1, out_AOI222X2, out_AOI22X1, 
        out_AOI22X2, out_AOINVX1, out_AOINVX2, out_AOINVX4, out_DELLN1X2, 
        out_DELLN2X2, out_DELLN3X2, out_IBUFFX16, out_IBUFFX2, out_IBUFFX32, 
        out_IBUFFX4, out_IBUFFX8, out_INVX0, out_INVX16, out_INVX1, out_INVX2, 
        out_INVX32, out_INVX4, out_INVX8, out_MUX21X1, out_MUX21X2, 
        out_MUX41X1, out_MUX41X2, out_NAND2X0, out_NAND2X1, out_NAND2X2, 
        out_NAND2X4, out_NAND3X0, out_NAND3X1, out_NAND3X2, out_NAND3X4, 
        out_NAND4X0, out_NAND4X1, out_NBUFFX16, out_NBUFFX2, out_NBUFFX32, 
        out_NBUFFX4, out_NBUFFX8, out_NOR2X0, out_NOR2X1, out_NOR2X2, 
        out_NOR2X4, out_NOR3X0, out_NOR3X1, out_NOR3X2, out_NOR3X4, out_NOR4X0, 
        out_NOR4X1, out_OA21X1, out_OA21X2, out_OA221X1, out_OA221X2, 
        out_OA222X1, out_OA222X2, out_OA22X1, out_OA22X2, out_OAI21X1, 
        out_OAI21X2, out_OAI221X1, out_OAI221X2, out_OAI222X1, out_OAI222X2, 
        out_OAI22X1, out_OAI22X2, out_OR2X1, out_OR2X2, out_OR2X4, out_OR3X1, 
        out_OR3X2, out_OR3X4, out_OR4X1, out_OR4X2, out_OR4X4, out_XNOR2X1, 
        out_XNOR2X2, out_XNOR3X1, out_XNOR3X2, out_XOR2X1, out_XOR2X2, 
        out_XOR3X1, out_XOR3X2, out_TIEL, out_TIEH, out_FADDX1_S, 
        out_FADDX1_CO, out_FADDX2_S, out_FADDX2_CO, out_HADDX1_SO, 
        out_HADDX1_C1, out_HADDX2_SO, out_HADDX2_C1, out_DEC24X1_Q0, 
        out_DEC24X1_Q1, out_DEC24X1_Q2, out_DEC24X1_Q3, out_DEC24X2_Q0, 
        out_DEC24X2_Q1, out_DEC24X2_Q2, out_DEC24X2_Q3 );
  input in_INP, in_IN1, in_IN2, in_IN3, in_IN4, in_IN5, in_IN6, in_S, in_S0,
         in_S1, in_A, in_B, in_CI, in_A0, in_B0;
  output out_AND2X1, out_AND2X2, out_AND2X4, out_AND3X1, out_AND3X2,
         out_AND3X4, out_AND4X1, out_AND4X2, out_AND4X4, out_AO21X1,
         out_AO21X2, out_AO221X1, out_AO221X2, out_AO222X1, out_AO222X2,
         out_AO22X1, out_AO22X2, out_AOBUFX1, out_AOBUFX2, out_AOBUFX4,
         out_AOI21X1, out_AOI21X2, out_AOI221X1, out_AOI221X2, out_AOI222X1,
         out_AOI222X2, out_AOI22X1, out_AOI22X2, out_AOINVX1, out_AOINVX2,
         out_AOINVX4, out_DELLN1X2, out_DELLN2X2, out_DELLN3X2, out_IBUFFX16,
         out_IBUFFX2, out_IBUFFX32, out_IBUFFX4, out_IBUFFX8, out_INVX0,
         out_INVX16, out_INVX1, out_INVX2, out_INVX32, out_INVX4, out_INVX8,
         out_MUX21X1, out_MUX21X2, out_MUX41X1, out_MUX41X2, out_NAND2X0,
         out_NAND2X1, out_NAND2X2, out_NAND2X4, out_NAND3X0, out_NAND3X1,
         out_NAND3X2, out_NAND3X4, out_NAND4X0, out_NAND4X1, out_NBUFFX16,
         out_NBUFFX2, out_NBUFFX32, out_NBUFFX4, out_NBUFFX8, out_NOR2X0,
         out_NOR2X1, out_NOR2X2, out_NOR2X4, out_NOR3X0, out_NOR3X1,
         out_NOR3X2, out_NOR3X4, out_NOR4X0, out_NOR4X1, out_OA21X1,
         out_OA21X2, out_OA221X1, out_OA221X2, out_OA222X1, out_OA222X2,
         out_OA22X1, out_OA22X2, out_OAI21X1, out_OAI21X2, out_OAI221X1,
         out_OAI221X2, out_OAI222X1, out_OAI222X2, out_OAI22X1, out_OAI22X2,
         out_OR2X1, out_OR2X2, out_OR2X4, out_OR3X1, out_OR3X2, out_OR3X4,
         out_OR4X1, out_OR4X2, out_OR4X4, out_XNOR2X1, out_XNOR2X2,
         out_XNOR3X1, out_XNOR3X2, out_XOR2X1, out_XOR2X2, out_XOR3X1,
         out_XOR3X2, out_TIEL, out_TIEH, out_FADDX1_S, out_FADDX1_CO,
         out_FADDX2_S, out_FADDX2_CO, out_HADDX1_SO, out_HADDX1_C1,
         out_HADDX2_SO, out_HADDX2_C1, out_DEC24X1_Q0, out_DEC24X1_Q1,
         out_DEC24X1_Q2, out_DEC24X1_Q3, out_DEC24X2_Q0, out_DEC24X2_Q1,
         out_DEC24X2_Q2, out_DEC24X2_Q3;
  wire   in_INP, out_AND3X4, out_AND4X4, out_AO222X2, out_AOI21X2,
         out_AOI221X2, out_AOI22X2, out_NOR2X4, out_NOR3X4, out_NOR4X1,
         out_OA21X2, out_OA221X2, out_OA222X2, out_OA22X2, out_XOR3X2,
         out_HADDX2_C1, out_DEC24X2_Q1, out_DEC24X2_Q2, out_DEC24X2_Q3,
         out_AOINVX1, out_OR4X1, out_OR3X1, out_OR2X1, out_OAI21X1,
         out_OAI221X1, out_OAI222X1, out_OAI22X1, out_XNOR3X1, out_XOR2X1,
         out_AO221X1, out_AOI222X1, out_AO22X1, out_AO21X1, out_NAND4X0,
         out_NAND3X0, out_NAND2X0, out_MUX21X1, out_MUX41X1, out_FADDX1_S,
         out_FADDX1_CO, out_HADDX1_SO, out_XNOR2X1, n69, n70, n71, n72, n73,
         n74, n75, n76, n77, n78, n79, n80, n81, n82, n83, n84, n85, n86, n87,
         n88, n89, n90, n91, n92, n93, n94, n95, n96, n97, n98, n99, n100,
         n101, n102, n103, n104, n105, n106, n107, n108;
  assign out_NBUFFX8 = in_INP;
  assign out_NBUFFX4 = in_INP;
  assign out_NBUFFX32 = in_INP;
  assign out_NBUFFX2 = in_INP;
  assign out_NBUFFX16 = in_INP;
  assign out_DELLN3X2 = in_INP;
  assign out_DELLN2X2 = in_INP;
  assign out_DELLN1X2 = in_INP;
  assign out_AOBUFX4 = in_INP;
  assign out_AOBUFX2 = in_INP;
  assign out_AOBUFX1 = in_INP;
  assign out_AND3X2 = out_AND3X4;
  assign out_AND3X1 = out_AND3X4;
  assign out_AND4X2 = out_AND4X4;
  assign out_AND4X1 = out_AND4X4;
  assign out_AO222X1 = out_AO222X2;
  assign out_AOI21X1 = out_AOI21X2;
  assign out_AOI221X1 = out_AOI221X2;
  assign out_AOI22X1 = out_AOI22X2;
  assign out_DEC24X2_Q0 = out_NOR2X4;
  assign out_DEC24X1_Q0 = out_NOR2X4;
  assign out_NOR2X2 = out_NOR2X4;
  assign out_NOR2X1 = out_NOR2X4;
  assign out_NOR2X0 = out_NOR2X4;
  assign out_NOR3X2 = out_NOR3X4;
  assign out_NOR3X1 = out_NOR3X4;
  assign out_NOR3X0 = out_NOR3X4;
  assign out_NOR4X0 = out_NOR4X1;
  assign out_OA21X1 = out_OA21X2;
  assign out_OA221X1 = out_OA221X2;
  assign out_OA222X1 = out_OA222X2;
  assign out_OA22X1 = out_OA22X2;
  assign out_XOR3X1 = out_XOR3X2;
  assign out_HADDX1_C1 = out_HADDX2_C1;
  assign out_DEC24X1_Q1 = out_DEC24X2_Q1;
  assign out_DEC24X1_Q2 = out_DEC24X2_Q2;
  assign out_DEC24X1_Q3 = out_DEC24X2_Q3;
  assign out_AND2X4 = out_DEC24X2_Q3;
  assign out_AND2X2 = out_DEC24X2_Q3;
  assign out_AND2X1 = out_DEC24X2_Q3;
  assign out_INVX8 = out_AOINVX1;
  assign out_INVX4 = out_AOINVX1;
  assign out_INVX32 = out_AOINVX1;
  assign out_INVX2 = out_AOINVX1;
  assign out_INVX1 = out_AOINVX1;
  assign out_INVX16 = out_AOINVX1;
  assign out_INVX0 = out_AOINVX1;
  assign out_IBUFFX8 = out_AOINVX1;
  assign out_IBUFFX4 = out_AOINVX1;
  assign out_IBUFFX32 = out_AOINVX1;
  assign out_IBUFFX2 = out_AOINVX1;
  assign out_IBUFFX16 = out_AOINVX1;
  assign out_AOINVX4 = out_AOINVX1;
  assign out_AOINVX2 = out_AOINVX1;
  assign out_OR4X4 = out_OR4X1;
  assign out_OR4X2 = out_OR4X1;
  assign out_OR3X4 = out_OR3X1;
  assign out_OR3X2 = out_OR3X1;
  assign out_OR2X4 = out_OR2X1;
  assign out_OR2X2 = out_OR2X1;
  assign out_OAI21X2 = out_OAI21X1;
  assign out_OAI221X2 = out_OAI221X1;
  assign out_OAI222X2 = out_OAI222X1;
  assign out_OAI22X2 = out_OAI22X1;
  assign out_XNOR3X2 = out_XNOR3X1;
  assign out_XOR2X2 = out_XOR2X1;
  assign out_AO221X2 = out_AO221X1;
  assign out_AOI222X2 = out_AOI222X1;
  assign out_AO22X2 = out_AO22X1;
  assign out_AO21X2 = out_AO21X1;
  assign out_NAND4X1 = out_NAND4X0;
  assign out_NAND3X4 = out_NAND3X0;
  assign out_NAND3X2 = out_NAND3X0;
  assign out_NAND3X1 = out_NAND3X0;
  assign out_NAND2X4 = out_NAND2X0;
  assign out_NAND2X2 = out_NAND2X0;
  assign out_NAND2X1 = out_NAND2X0;
  assign out_MUX21X2 = out_MUX21X1;
  assign out_MUX41X2 = out_MUX41X1;
  assign out_FADDX2_S = out_FADDX1_S;
  assign out_FADDX2_CO = out_FADDX1_CO;
  assign out_HADDX2_SO = out_HADDX1_SO;
  assign out_XNOR2X2 = out_XNOR2X1;

  TIEL inst_TIEL ( .ZN(out_TIEL) );
  TIEH inst_TIEH ( .Z(out_TIEH) );
  INVX1 U87 ( .INP(in_IN2), .ZN(n103) );
  NOR2X1 U88 ( .IN1(in_IN1), .IN2(n103), .QN(out_DEC24X2_Q1) );
  INVX1 U89 ( .INP(in_IN1), .ZN(n105) );
  NOR2X1 U90 ( .IN1(in_IN2), .IN2(n105), .QN(out_DEC24X2_Q2) );
  NOR2X1 U91 ( .IN1(in_IN2), .IN2(in_IN1), .QN(out_NOR2X4) );
  NOR2X1 U92 ( .IN1(n103), .IN2(n105), .QN(out_DEC24X2_Q3) );
  NOR2X1 U93 ( .IN1(out_NOR2X4), .IN2(out_DEC24X2_Q3), .QN(out_XOR2X1) );
  INVX1 U94 ( .INP(out_XOR2X1), .ZN(out_XNOR2X1) );
  INVX1 U95 ( .INP(in_IN3), .ZN(n77) );
  NOR2X1 U96 ( .IN1(out_XOR2X1), .IN2(n77), .QN(n70) );
  NOR2X1 U97 ( .IN1(in_IN3), .IN2(out_XNOR2X1), .QN(n69) );
  NOR2X1 U98 ( .IN1(n70), .IN2(n69), .QN(out_XNOR3X1) );
  INVX1 U99 ( .INP(out_XNOR3X1), .ZN(out_XOR3X2) );
  INVX1 U100 ( .INP(out_DEC24X2_Q3), .ZN(out_NAND2X0) );
  NOR2X1 U101 ( .IN1(n77), .IN2(out_NAND2X0), .QN(out_AND3X4) );
  INVX1 U102 ( .INP(out_AND3X4), .ZN(out_NAND3X0) );
  INVX1 U103 ( .INP(in_IN4), .ZN(n71) );
  NOR2X1 U104 ( .IN1(n71), .IN2(out_NAND3X0), .QN(out_AND4X4) );
  NOR2X1 U105 ( .IN1(in_IN3), .IN2(out_DEC24X2_Q3), .QN(out_AOI21X2) );
  NOR2X1 U106 ( .IN1(n77), .IN2(n71), .QN(n72) );
  NOR2X1 U107 ( .IN1(n72), .IN2(out_DEC24X2_Q3), .QN(out_AOI22X2) );
  INVX1 U108 ( .INP(out_AOI22X2), .ZN(out_AO22X1) );
  NOR2X1 U109 ( .IN1(out_AO22X1), .IN2(in_IN5), .QN(out_AOI221X2) );
  INVX1 U110 ( .INP(in_IN5), .ZN(n79) );
  INVX1 U111 ( .INP(in_IN6), .ZN(n73) );
  NOR2X1 U112 ( .IN1(n79), .IN2(n73), .QN(n74) );
  NOR2X1 U113 ( .IN1(n74), .IN2(out_AO22X1), .QN(out_AOI222X1) );
  INVX1 U114 ( .INP(out_AOI222X1), .ZN(out_AO222X2) );
  INVX1 U115 ( .INP(in_A0), .ZN(n76) );
  INVX1 U116 ( .INP(in_B0), .ZN(n75) );
  NOR2X1 U117 ( .IN1(n76), .IN2(n75), .QN(out_HADDX2_C1) );
  INVX1 U118 ( .INP(out_NOR2X4), .ZN(out_OR2X1) );
  NOR2X1 U119 ( .IN1(in_IN3), .IN2(out_OR2X1), .QN(out_NOR3X4) );
  INVX1 U120 ( .INP(out_NOR3X4), .ZN(out_OR3X1) );
  NOR2X1 U121 ( .IN1(in_IN4), .IN2(out_OR3X1), .QN(out_NOR4X1) );
  NOR2X1 U122 ( .IN1(out_NOR2X4), .IN2(n77), .QN(out_OA21X2) );
  NOR2X1 U123 ( .IN1(in_IN3), .IN2(in_IN4), .QN(n78) );
  NOR2X1 U124 ( .IN1(n78), .IN2(out_NOR2X4), .QN(out_OA22X2) );
  INVX1 U125 ( .INP(out_OA22X2), .ZN(out_OAI22X1) );
  NOR2X1 U126 ( .IN1(out_OAI22X1), .IN2(n79), .QN(out_OA221X2) );
  NOR2X1 U127 ( .IN1(in_IN5), .IN2(in_IN6), .QN(n80) );
  NOR2X1 U128 ( .IN1(n80), .IN2(out_OAI22X1), .QN(out_OA222X2) );
  NOR2X1 U129 ( .IN1(in_A0), .IN2(in_B0), .QN(n81) );
  NOR2X1 U130 ( .IN1(n81), .IN2(out_HADDX2_C1), .QN(out_HADDX1_SO) );
  INVX1 U131 ( .INP(in_B), .ZN(n82) );
  INVX1 U132 ( .INP(in_A), .ZN(n83) );
  NOR2X1 U133 ( .IN1(n82), .IN2(n83), .QN(n87) );
  NOR2X1 U134 ( .IN1(in_A), .IN2(n82), .QN(n85) );
  NOR2X1 U135 ( .IN1(in_B), .IN2(n83), .QN(n84) );
  NOR2X1 U136 ( .IN1(n85), .IN2(n84), .QN(n89) );
  INVX1 U137 ( .INP(in_CI), .ZN(n86) );
  NOR2X1 U138 ( .IN1(n89), .IN2(n86), .QN(n91) );
  NOR2X1 U139 ( .IN1(n87), .IN2(n91), .QN(n88) );
  INVX1 U140 ( .INP(n88), .ZN(out_FADDX1_CO) );
  INVX1 U141 ( .INP(n89), .ZN(n90) );
  NOR2X1 U142 ( .IN1(n90), .IN2(in_CI), .QN(n92) );
  NOR2X1 U143 ( .IN1(n92), .IN2(n91), .QN(out_FADDX1_S) );
  NOR2X1 U144 ( .IN1(in_IN2), .IN2(in_S0), .QN(n94) );
  INVX1 U145 ( .INP(in_S0), .ZN(n97) );
  NOR2X1 U146 ( .IN1(in_IN4), .IN2(n97), .QN(n93) );
  NOR2X1 U147 ( .IN1(n94), .IN2(n93), .QN(n96) );
  INVX1 U148 ( .INP(in_S1), .ZN(n95) );
  NOR2X1 U149 ( .IN1(n96), .IN2(n95), .QN(n102) );
  NOR2X1 U150 ( .IN1(n97), .IN2(in_IN3), .QN(n99) );
  NOR2X1 U151 ( .IN1(in_IN1), .IN2(in_S0), .QN(n98) );
  NOR2X1 U152 ( .IN1(n99), .IN2(n98), .QN(n100) );
  NOR2X1 U153 ( .IN1(n100), .IN2(in_S1), .QN(n101) );
  NOR2X1 U154 ( .IN1(n102), .IN2(n101), .QN(out_MUX41X1) );
  INVX1 U155 ( .INP(in_S), .ZN(n104) );
  NOR2X1 U156 ( .IN1(n104), .IN2(n103), .QN(n107) );
  NOR2X1 U157 ( .IN1(in_S), .IN2(n105), .QN(n106) );
  NOR2X1 U158 ( .IN1(n107), .IN2(n106), .QN(n108) );
  INVX1 U159 ( .INP(n108), .ZN(out_MUX21X1) );
  INVX1 U160 ( .INP(out_AND4X4), .ZN(out_NAND4X0) );
  INVX1 U161 ( .INP(out_AOI21X2), .ZN(out_AO21X1) );
  INVX1 U162 ( .INP(out_AOI221X2), .ZN(out_AO221X1) );
  INVX1 U163 ( .INP(out_OA222X2), .ZN(out_OAI222X1) );
  INVX1 U164 ( .INP(out_OA221X2), .ZN(out_OAI221X1) );
  INVX1 U165 ( .INP(out_OA21X2), .ZN(out_OAI21X1) );
  INVX1 U166 ( .INP(out_NOR4X1), .ZN(out_OR4X1) );
  INVX1 U167 ( .INP(in_INP), .ZN(out_AOINVX1) );
endmodule

