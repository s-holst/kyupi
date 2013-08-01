
module SAED90cells ( 
in_INP, in_IN1, in_IN2, in_IN3, in_IN4, in_IN5, in_IN6, in_S, in_S0, in_S1, in_A, in_B, in_CI, in_A0, in_B0,
out_AND2X1, out_AND2X2, out_AND2X4, 
out_AND3X1, out_AND3X2, out_AND3X4, 
out_AND4X1, out_AND4X2, out_AND4X4, 
out_AO21X1, out_AO21X2, 
out_AO221X1, out_AO221X2, 
out_AO222X1, out_AO222X2, 
out_AO22X1, out_AO22X2, 
out_AOBUFX1, out_AOBUFX2, out_AOBUFX4,
out_AOI21X1, out_AOI21X2, 
out_AOI221X1, out_AOI221X2, 
out_AOI222X1, out_AOI222X2, 
out_AOI22X1, out_AOI22X2, 
out_AOINVX1, out_AOINVX2, out_AOINVX4, 
out_DELLN1X2, out_DELLN2X2, out_DELLN3X2, 
out_IBUFFX16, out_IBUFFX2, out_IBUFFX32, out_IBUFFX4, out_IBUFFX8, 
out_INVX0, out_INVX16, out_INVX1, out_INVX2, out_INVX32, out_INVX4, out_INVX8, 
out_MUX21X1, out_MUX21X2, 
out_MUX41X1, out_MUX41X2, 
out_NAND2X0, out_NAND2X1, out_NAND2X2, out_NAND2X4, 
out_NAND3X0, out_NAND3X1, out_NAND3X2, out_NAND3X4, 
out_NAND4X0, out_NAND4X1, 
out_NBUFFX16, out_NBUFFX2, out_NBUFFX32, out_NBUFFX4, out_NBUFFX8, 
out_NOR2X0, out_NOR2X1, out_NOR2X2, out_NOR2X4, 
out_NOR3X0, out_NOR3X1, out_NOR3X2, out_NOR3X4, 
out_NOR4X0, out_NOR4X1, 
out_OA21X1, out_OA21X2, 
out_OA221X1, out_OA221X2, 
out_OA222X1, out_OA222X2, 
out_OA22X1, out_OA22X2, 
out_OAI21X1, out_OAI21X2, 
out_OAI221X1, out_OAI221X2, 
out_OAI222X1, out_OAI222X2, 
out_OAI22X1, out_OAI22X2, 
out_OR2X1, out_OR2X2, out_OR2X4,
out_OR3X1, out_OR3X2, out_OR3X4, 
out_OR4X1, out_OR4X2, out_OR4X4, 
out_XNOR2X1, out_XNOR2X2, 
out_XNOR3X1, out_XNOR3X2, 
out_XOR2X1, out_XOR2X2, 
out_XOR3X1, out_XOR3X2, 
out_TIEL, out_TIEH, 
out_FADDX1_S, out_FADDX1_CO,
out_FADDX2_S, out_FADDX2_CO,
out_HADDX1_SO, out_HADDX1_C1,
out_HADDX2_SO, out_HADDX2_C1,
out_DEC24X1_Q0, out_DEC24X1_Q1, out_DEC24X1_Q2, out_DEC24X1_Q3,
out_DEC24X2_Q0, out_DEC24X2_Q1, out_DEC24X2_Q2, out_DEC24X2_Q3
);


input in_INP, in_IN1, in_IN2, in_IN3, in_IN4, in_IN5, in_IN6, in_S, in_S0, in_S1, in_A, in_B, in_CI, in_A0, in_B0;
output 
out_AND2X1, out_AND2X2, out_AND2X4, 
out_AND3X1, out_AND3X2, out_AND3X4, 
out_AND4X1, out_AND4X2, out_AND4X4, 
out_AO21X1, out_AO21X2, 
out_AO221X1, out_AO221X2, 
out_AO222X1, out_AO222X2, 
out_AO22X1, out_AO22X2, 
out_AOBUFX1, out_AOBUFX2, out_AOBUFX4,
out_AOI21X1, out_AOI21X2, 
out_AOI221X1, out_AOI221X2, 
out_AOI222X1, out_AOI222X2, 
out_AOI22X1, out_AOI22X2, 
out_AOINVX1, out_AOINVX2, out_AOINVX4, 
out_DELLN1X2, out_DELLN2X2, out_DELLN3X2, 
out_IBUFFX16, out_IBUFFX2, out_IBUFFX32, out_IBUFFX4, out_IBUFFX8, 
out_INVX0, out_INVX16, out_INVX1, out_INVX2, out_INVX32, out_INVX4, out_INVX8, 
out_MUX21X1, out_MUX21X2, 
out_MUX41X1, out_MUX41X2, 
out_NAND2X0, out_NAND2X1, out_NAND2X2, out_NAND2X4, 
out_NAND3X0, out_NAND3X1, out_NAND3X2, out_NAND3X4, 
out_NAND4X0, out_NAND4X1, 
out_NBUFFX16, out_NBUFFX2, out_NBUFFX32, out_NBUFFX4, out_NBUFFX8, 
out_NOR2X0, out_NOR2X1, out_NOR2X2, out_NOR2X4, 
out_NOR3X0, out_NOR3X1, out_NOR3X2, out_NOR3X4, 
out_NOR4X0, out_NOR4X1, 
out_OA21X1, out_OA21X2, 
out_OA221X1, out_OA221X2, 
out_OA222X1, out_OA222X2, 
out_OA22X1, out_OA22X2, 
out_OAI21X1, out_OAI21X2, 
out_OAI221X1, out_OAI221X2, 
out_OAI222X1, out_OAI222X2, 
out_OAI22X1, out_OAI22X2, 
out_OR2X1, out_OR2X2, out_OR2X4,
out_OR3X1, out_OR3X2, out_OR3X4, 
out_OR4X1, out_OR4X2, out_OR4X4, 
out_XNOR2X1, out_XNOR2X2, 
out_XNOR3X1, out_XNOR3X2, 
out_XOR2X1, out_XOR2X2, 
out_XOR3X1, out_XOR3X2, 
out_TIEL, out_TIEH, 
out_FADDX1_S, out_FADDX1_CO,
out_FADDX2_S, out_FADDX2_CO,
out_HADDX1_SO, out_HADDX1_C1,
out_HADDX2_SO, out_HADDX2_C1,
out_DEC24X1_Q0, out_DEC24X1_Q1, out_DEC24X1_Q2, out_DEC24X1_Q3,
out_DEC24X2_Q0, out_DEC24X2_Q1, out_DEC24X2_Q2, out_DEC24X2_Q3;

  AND2X1 inst_AND2X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.Q(out_AND2X1));
  AND2X2 inst_AND2X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.Q(out_AND2X2));
  AND2X4 inst_AND2X4 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.Q(out_AND2X4));
  AND3X1 inst_AND3X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.Q(out_AND3X1));
  AND3X2 inst_AND3X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.Q(out_AND3X2));
  AND3X4 inst_AND3X4 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.Q(out_AND3X4));
  AND4X1 inst_AND4X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.Q(out_AND4X1));
  AND4X2 inst_AND4X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.Q(out_AND4X2));
  AND4X4 inst_AND4X4 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.Q(out_AND4X4));
  AO21X1 inst_AO21X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.Q(out_AO21X1));
  AO21X2 inst_AO21X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.Q(out_AO21X2));
  AO221X1 inst_AO221X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.IN5(in_IN5) ,.Q(out_AO221X1));
  AO221X2 inst_AO221X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.IN5(in_IN5) ,.Q(out_AO221X2));
  AO222X1 inst_AO222X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.IN5(in_IN5) ,.IN6(in_IN6) ,.Q(out_AO222X1));
  AO222X2 inst_AO222X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.IN5(in_IN5) ,.IN6(in_IN6) ,.Q(out_AO222X2));
  AO22X1 inst_AO22X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.Q(out_AO22X1));
  AO22X2 inst_AO22X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.Q(out_AO22X2));
  AOBUFX1 inst_AOBUFX1 ( .INP(in_INP) ,.Z(out_AOBUFX1));
  AOBUFX2 inst_AOBUFX2 ( .INP(in_INP) ,.Z(out_AOBUFX2));
  AOBUFX4 inst_AOBUFX4 ( .INP(in_INP) ,.Z(out_AOBUFX4));
  AOI21X1 inst_AOI21X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.QN(out_AOI21X1));
  AOI21X2 inst_AOI21X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.QN(out_AOI21X2));
  AOI221X1 inst_AOI221X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.IN5(in_IN5) ,.QN(out_AOI221X1));
  AOI221X2 inst_AOI221X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.IN5(in_IN5) ,.QN(out_AOI221X2));
  AOI222X1 inst_AOI222X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.IN5(in_IN5) ,.IN6(in_IN6) ,.QN(out_AOI222X1));
  AOI222X2 inst_AOI222X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.IN5(in_IN5) ,.IN6(in_IN6) ,.QN(out_AOI222X2));
  AOI22X1 inst_AOI22X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.QN(out_AOI22X1));
  AOI22X2 inst_AOI22X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.QN(out_AOI22X2));
  AOINVX1 inst_AOINVX1 ( .INP(in_INP) ,.ZN(out_AOINVX1));
  AOINVX2 inst_AOINVX2 ( .INP(in_INP) ,.ZN(out_AOINVX2));
  AOINVX4 inst_AOINVX4 ( .INP(in_INP) ,.ZN(out_AOINVX4));
  DELLN1X2 inst_DELLN1X2 ( .INP(in_INP) ,.Z(out_DELLN1X2));
  DELLN2X2 inst_DELLN2X2 ( .INP(in_INP) ,.Z(out_DELLN2X2));
  DELLN3X2 inst_DELLN3X2 ( .INP(in_INP) ,.Z(out_DELLN3X2));
  IBUFFX16 inst_IBUFFX16 ( .INP(in_INP) ,.ZN(out_IBUFFX16));
  IBUFFX2 inst_IBUFFX2 ( .INP(in_INP) ,.ZN(out_IBUFFX2));
  IBUFFX32 inst_IBUFFX32 ( .INP(in_INP) ,.ZN(out_IBUFFX32));
  IBUFFX4 inst_IBUFFX4 ( .INP(in_INP) ,.ZN(out_IBUFFX4));
  IBUFFX8 inst_IBUFFX8 ( .INP(in_INP) ,.ZN(out_IBUFFX8));
  INVX0 inst_INVX0 ( .INP(in_INP) ,.ZN(out_INVX0));
  INVX16 inst_INVX16 ( .INP(in_INP) ,.ZN(out_INVX16));
  INVX1 inst_INVX1 ( .INP(in_INP) ,.ZN(out_INVX1));
  INVX2 inst_INVX2 ( .INP(in_INP) ,.ZN(out_INVX2));
  INVX32 inst_INVX32 ( .INP(in_INP) ,.ZN(out_INVX32));
  INVX4 inst_INVX4 ( .INP(in_INP) ,.ZN(out_INVX4));
  INVX8 inst_INVX8 ( .INP(in_INP) ,.ZN(out_INVX8));
  MUX21X1 inst_MUX21X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.S(in_S) ,.Q(out_MUX21X1));
  MUX21X2 inst_MUX21X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.S(in_S) ,.Q(out_MUX21X2));
  MUX41X1 inst_MUX41X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.S0(in_S0) ,.S1(in_S1) ,.Q(out_MUX41X1));
  MUX41X2 inst_MUX41X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.S0(in_S0) ,.S1(in_S1) ,.Q(out_MUX41X2));
  NAND2X0 inst_NAND2X0 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.QN(out_NAND2X0));
  NAND2X1 inst_NAND2X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.QN(out_NAND2X1));
  NAND2X2 inst_NAND2X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.QN(out_NAND2X2));
  NAND2X4 inst_NAND2X4 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.QN(out_NAND2X4));
  NAND3X0 inst_NAND3X0 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.QN(out_NAND3X0));
  NAND3X1 inst_NAND3X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.QN(out_NAND3X1));
  NAND3X2 inst_NAND3X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.QN(out_NAND3X2));
  NAND3X4 inst_NAND3X4 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.QN(out_NAND3X4));
  NAND4X0 inst_NAND4X0 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.QN(out_NAND4X0));
  NAND4X1 inst_NAND4X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.QN(out_NAND4X1));
  NBUFFX16 inst_NBUFFX16 ( .INP(in_INP) ,.Z(out_NBUFFX16));
  NBUFFX2 inst_NBUFFX2 ( .INP(in_INP) ,.Z(out_NBUFFX2));
  NBUFFX32 inst_NBUFFX32 ( .INP(in_INP) ,.Z(out_NBUFFX32));
  NBUFFX4 inst_NBUFFX4 ( .INP(in_INP) ,.Z(out_NBUFFX4));
  NBUFFX8 inst_NBUFFX8 ( .INP(in_INP) ,.Z(out_NBUFFX8));
  NOR2X0 inst_NOR2X0 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.QN(out_NOR2X0));
  NOR2X1 inst_NOR2X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.QN(out_NOR2X1));
  NOR2X2 inst_NOR2X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.QN(out_NOR2X2));
  NOR2X4 inst_NOR2X4 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.QN(out_NOR2X4));
  NOR3X0 inst_NOR3X0 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.QN(out_NOR3X0));
  NOR3X1 inst_NOR3X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.QN(out_NOR3X1));
  NOR3X2 inst_NOR3X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.QN(out_NOR3X2));
  NOR3X4 inst_NOR3X4 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.QN(out_NOR3X4));
  NOR4X0 inst_NOR4X0 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.QN(out_NOR4X0));
  NOR4X1 inst_NOR4X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.QN(out_NOR4X1));
  OA21X1 inst_OA21X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.Q(out_OA21X1));
  OA21X2 inst_OA21X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.Q(out_OA21X2));
  OA221X1 inst_OA221X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.IN5(in_IN5) ,.Q(out_OA221X1));
  OA221X2 inst_OA221X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.IN5(in_IN5) ,.Q(out_OA221X2));
  OA222X1 inst_OA222X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.IN5(in_IN5) ,.IN6(in_IN6) ,.Q(out_OA222X1));
  OA222X2 inst_OA222X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.IN5(in_IN5) ,.IN6(in_IN6) ,.Q(out_OA222X2));
  OA22X1 inst_OA22X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.Q(out_OA22X1));
  OA22X2 inst_OA22X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.Q(out_OA22X2));
  OAI21X1 inst_OAI21X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.QN(out_OAI21X1));
  OAI21X2 inst_OAI21X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.QN(out_OAI21X2));
  OAI221X1 inst_OAI221X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.IN5(in_IN5) ,.QN(out_OAI221X1));
  OAI221X2 inst_OAI221X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.IN5(in_IN5) ,.QN(out_OAI221X2));
  OAI222X1 inst_OAI222X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.IN5(in_IN5) ,.IN6(in_IN6) ,.QN(out_OAI222X1));
  OAI222X2 inst_OAI222X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.IN5(in_IN5) ,.IN6(in_IN6) ,.QN(out_OAI222X2));
  OAI22X1 inst_OAI22X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.QN(out_OAI22X1));
  OAI22X2 inst_OAI22X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.QN(out_OAI22X2));
  OR2X1 inst_OR2X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.Q(out_OR2X1));
  OR2X2 inst_OR2X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.Q(out_OR2X2));
  OR2X4 inst_OR2X4 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.Q(out_OR2X4));
  OR3X1 inst_OR3X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.Q(out_OR3X1));
  OR3X2 inst_OR3X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.Q(out_OR3X2));
  OR3X4 inst_OR3X4 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.Q(out_OR3X4));
  OR4X1 inst_OR4X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.Q(out_OR4X1));
  OR4X2 inst_OR4X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.Q(out_OR4X2));
  OR4X4 inst_OR4X4 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.IN4(in_IN4) ,.Q(out_OR4X4));
  XNOR2X1 inst_XNOR2X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.Q(out_XNOR2X1));
  XNOR2X2 inst_XNOR2X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.Q(out_XNOR2X2));
  XNOR3X1 inst_XNOR3X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.Q(out_XNOR3X1));
  XNOR3X2 inst_XNOR3X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.Q(out_XNOR3X2));
  XOR2X1 inst_XOR2X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.Q(out_XOR2X1));
  XOR2X2 inst_XOR2X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.Q(out_XOR2X2));
  XOR3X1 inst_XOR3X1 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.Q(out_XOR3X1));
  XOR3X2 inst_XOR3X2 ( .IN1(in_IN1) ,.IN2(in_IN2) ,.IN3(in_IN3) ,.Q(out_XOR3X2));
  // constants
  TIEL inst_TIEL (.ZN(out_TIEL));
  TIEH inst_TIEH (.Z(out_TIEH));
  // multi-output cells
  DEC24X1 inst_DEC24X1 ( .IN1(in_IN1) ,.IN2(in_IN2), .Q0(out_DEC24X1_Q0), .Q1(out_DEC24X1_Q1), .Q2(out_DEC24X1_Q2), .Q3(out_DEC24X1_Q3));
  DEC24X2 inst_DEC24X2 ( .IN1(in_IN1) ,.IN2(in_IN2), .Q0(out_DEC24X2_Q0), .Q1(out_DEC24X2_Q1), .Q2(out_DEC24X2_Q2), .Q3(out_DEC24X2_Q3));
  FADDX1 inst_FADDX1 ( .A(in_A) ,.B(in_B) ,.CI(in_CI) ,.S(out_FADDX1_S) ,.CO(out_FADDX1_CO));
  FADDX2 inst_FADDX2 ( .A(in_A) ,.B(in_B) ,.CI(in_CI) ,.S(out_FADDX2_S) ,.CO(out_FADDX2_CO));
  HADDX1 inst_HADDX1 ( .A0(in_A0) ,.B0(in_B0), .SO(out_HADDX1_SO) ,.C1(out_HADDX1_C1));
  HADDX2 inst_HADDX2 ( .A0(in_A0) ,.B0(in_B0), .SO(out_HADDX2_SO) ,.C1(out_HADDX2_C1));
endmodule


// OTHER

// module PGX1 (INQ1, INN, INP, INQ2);
// module PGX2 (INQ1, INN, INP, INQ2);
// module PGX4 (INQ1, INN, INP, INQ2);
// module PMT1 (G, D, S);
// module PMT2 (G, D, S);
// module PMT3 (G, D, S);
// module NMT1 (G, D, S);
// module NMT2 (G, D, S);
// module NMT3 (G, D, S);

// PHYSICAL

// module ANTENNA (INP);

// module BUSKP (INP);
// module CLOAD1 (INP);
// module DCAP();

// module DHFILLHLH2();
// module DHFILLHLHLS11();
// module DHFILLLHL2();

// module HEAD2X16 (SLEEP, SLEEPOUT);
// module HEAD2X2 (SLEEP, SLEEPOUT);
// module HEAD2X32 (SLEEP, SLEEPOUT);
// module HEAD2X4 (SLEEP, SLEEPOUT);
// module HEAD2X8 (SLEEP, SLEEPOUT);
// module HEADX16 (SLEEP);
// module HEADX2 (SLEEP);
// module HEADX32 (SLEEP);
// module HEADX4 (SLEEP);
// module HEADX8 (SLEEP);

// module SHFILL2();
// module SHFILL3();
// module SHFILL1();
// module SHFILL64();
// module SHFILL128();

// TRISTATE

// module BSLEX1 (INOUT1, ENB, INOUT2);
// module BSLEX2 (INOUT1, ENB, INOUT2);
// module BSLEX4 (INOUT1, ENB, INOUT2);

// CLOCKING

// module CGLNPRX2 (SE,EN,CLK,GCLK);
// module CGLNPRX8 (SE,EN,CLK,GCLK);
// module CGLNPSX16 (SE,EN,CLK,GCLK);
// module CGLNPSX2 (SE,EN,CLK,GCLK);
// module CGLNPSX4 (SE,EN,CLK,GCLK);
// module CGLNPSX8 (SE,EN,CLK,GCLK);
// module CGLPPRX2 (SE,EN,CLK,GCLK);
// module CGLPPRX8 (SE,EN,CLK,GCLK);
// module CGLPPSX16 (SE,EN,CLK,GCLK);
// module CGLPPSX2 (SE,EN,CLK,GCLK);
// module CGLPPSX4 (SE,EN,CLK,GCLK);
// module CGLPPSX8 (SE,EN,CLK,GCLK);

// SEQUENTIAL

// module TNBUFFX16 (INP,ENB,Z);
// module TNBUFFX1 (INP,ENB,Z);
// module TNBUFFX2 (INP,ENB,Z);
// module TNBUFFX32 (INP,ENB,Z);
// module TNBUFFX4 (INP,ENB,Z);
// module TNBUFFX8 (INP,ENB,Z);
// module ISOLANDX1 (ISO,D,Q);
// module ISOLANDX2 (ISO,D,Q);
// module ISOLANDX4 (ISO,D,Q);
// module ISOLANDX8 (ISO,D,Q);
// module ISOLORX1 (ISO,D,Q);
// module ISOLORX2 (ISO,D,Q);
// module ISOLORX4 (ISO,D,Q);
// module ISOLORX8 (ISO,D,Q);
// module LNANDX1 (SIN,RIN,Q,QN);
// module LNANDX2 (SIN,RIN,Q,QN);
// module LSDNENX1 (ENB,D,Q);
// module LSDNENX2 (ENB,D,Q);
// module LSDNENX4 (ENB,D,Q);
// module LSDNENX8 (ENB,D,Q);
// module LSDNENCLX1 (ENB,D,Q);
// module LSDNENCLX2 (ENB,D,Q);
// module LSDNENCLX4 (ENB,D,Q);
// module LSDNENCLX8 (ENB,D,Q);
// module LSDNX1 (D,Q);
// module LSDNX2 (D,Q);
// module LSDNX4 (D,Q);
// module LSDNX8 (D,Q);
// module LSUPENX1 (ENB,D,Q);
// module LSUPENX2 (ENB,D,Q);
// module LSUPENX4 (ENB,D,Q);
// module LSUPENX8 (ENB,D,Q);
// module LSUPENCLX1 (ENB,D,Q);
// module LSUPENCLX2 (ENB,D,Q);
// module LSUPENCLX4 (ENB,D,Q);
// module LSUPENCLX8 (ENB,D,Q);
// module LSUPX1 (D,Q);
// module LSUPX2 (D,Q);
// module LSUPX4 (D,Q);
// module LSUPX8 (D,Q);

// module AODFFARX1 (D,CLK,RSTB,Q,QN);
// module AODFFARX2 (D,CLK,RSTB,Q,QN);
// module AODFFNARX1 (D,CLK,RSTB,Q,QN);
// module AODFFNARX2 (D,CLK,RSTB,Q,QN);

// module DFFARX1 (D,CLK,RSTB,Q,QN);
// module DFFARX2 (D,CLK,RSTB,Q,QN);
// module DFFASRX1 (D,CLK,RSTB,SETB,Q,QN);
// module DFFASRX2 (D,CLK,RSTB,SETB,Q,QN);
// module DFFASX1 (D,CLK,SETB,Q,QN);
// module DFFASX2 (D,CLK,SETB,Q,QN);
// module DFFNARX1 (D,CLK,RSTB,Q,QN);
// module DFFNARX2 (D,CLK,RSTB,Q,QN);
// module DFFNASRNX1 (D,CLK,RSTB,SETB,QN);
// module DFFNASRNX2 (D,CLK,RSTB,SETB,QN);
// module DFFNASRQX1 (D,CLK,RSTB,SETB,Q);
// module DFFNASRQX2 (D,CLK,RSTB,SETB,Q);
// module DFFNASRX1 (D,CLK,RSTB,SETB,Q,QN);
// module DFFNASRX2 (D,CLK,RSTB,SETB,Q,QN);
// module DFFNASX1 (D,CLK,SETB,Q,QN);
// module DFFNASX2 (D,CLK,SETB,Q,QN);
// module DFFNX1 (D,CLK,Q,QN);
// module DFFNX2 (D,CLK,Q,QN);
// module DFFSSRX1 (CLK,D,RSTB,SETB,Q,QN);
// module DFFSSRX2 (CLK,D,RSTB,SETB,Q,QN);
// module DFFX1 (D,CLK,Q,QN);
// module DFFX2 (D,CLK,Q,QN);

// module LARX1 (D,CLK,RSTB,Q,QN);
// module LARX2 (D,CLK,RSTB,Q,QN);
// module LASRNX1 (D,CLK,SETB,RSTB,QN);
// module LASRNX2 (D,CLK,SETB,RSTB,QN);
// module LASRQX1 (D,CLK,SETB,RSTB,Q);
// module LASRQX2 (D,CLK,SETB,RSTB,Q);
// module LASRX1 (D,CLK,SETB,RSTB,Q,QN);
// module LASRX2 (D,CLK,SETB,RSTB,Q,QN);
// module LASX1 (D,CLK,SETB,Q,QN);
// module LASX2 (D,CLK,SETB,Q,QN);
// module LATCHX1 (D,CLK,Q,QN);
// module LATCHX2 (D,CLK,Q,QN);

// module SDFFARX1 (D,CLK,RSTB,SE,SI,Q,QN);
// module SDFFARX2 (D,CLK,RSTB,SE,SI,Q,QN);
// module SDFFASRSX1 (D,CLK,RSTB,SETB,SE,SI,Q,QN,SO);
// module SDFFASRSX2 (D,CLK,RSTB,SETB,SE,SI,Q,QN,SO);
// module SDFFASRX1 (D,CLK,RSTB,SETB,SE,SI,Q,QN);
// module SDFFASRX2 (D,CLK,RSTB,SETB,SE,SI,Q,QN);
// module SDFFASX1 (D,CLK,SETB,SE,SI,Q,QN);
// module SDFFASX2 (D,CLK,SETB,SE,SI,Q,QN);
// module SDFFNARX1 (D,CLK,RSTB,SE,SI,Q,QN);
// module SDFFNARX2 (D,CLK,RSTB,SE,SI,Q,QN);
// module SDFFNASRX1 (D,CLK,RSTB,SETB,SE,SI,Q,QN);
// module SDFFNASRX2 (D,CLK,RSTB,SETB,SE,SI,Q,QN);
// module SDFFNASX1 (D,CLK,SETB,SE,SI,Q,QN);
// module SDFFNASX2 (D,CLK,SETB,SE,SI,Q,QN);
// module SDFFNX1 (D,CLK,SE,SI,Q,QN);
// module SDFFNX2 (D,CLK,SE,SI,Q,QN);
// module SDFFSSRX1 (CLK, D, RSTB, SETB, SI, SE, Q, QN);
// module SDFFSSRX2 (CLK, D, RSTB, SETB, SI, SE, Q, QN);
// module SDFFX1 (D,CLK,SE,SI,Q,QN);
// module SDFFX2 (D,CLK,SE,SI,Q,QN);

// module RDFFARX1 (D, CLK, RETN, RSTB, Q, QN);
// module RDFFARX2 (D, CLK, RETN, RSTB, Q, QN);
// module RDFFNARX1 (D, CLK, RETN, RSTB, Q, QN);
// module RDFFNARX2 (D, CLK, RETN, RSTB, Q, QN);
// module RSDFFARX1 (D, CLK, RETN, RSTB, SE, SI, Q, QN);
// module RSDFFARX2 (D, CLK, RETN, RSTB, SE, SI, Q, QN);
// module RSDFFNARX1 (D, CLK, RETN, RSTB, SE, SI, Q, QN);
// module RSDFFNARX2 (D, CLK, RETN, RSTB, SE, SI, Q, QN);
// module RDFFNX1 (D,CLK,RETN,Q,QN);
// module RDFFNX2 (D,CLK,RETN,Q,QN);
// module RDFFX1 (D,CLK,RETN,Q,QN);
// module RDFFX2 (D,CLK,RETN,Q,QN);
// module RSDFFNX1 (D,CLK,RETN,SE,SI,Q,QN);
// module RSDFFNX2 (D,CLK,RETN,SE,SI,Q,QN);
// module RSDFFX1 (D,CLK,RETN,SE,SI,Q,QN);
// module RSDFFX2 (D,CLK,RETN,SE,SI,Q,QN);
// module RDFFNSRASX1 (SETB, CLK, D, NRESTORE, Q, QN, SAVE );
// module RDFFNSRASX2 (SETB, CLK, D, NRESTORE, Q, QN, SAVE );
// module RDFFSRARX1 (RSTB, CLK, D, NRESTORE, Q, QN, SAVE);
// module RDFFSRARX2 (RSTB, CLK, D, NRESTORE, Q, QN, SAVE);
// module RDFFSRASRX1 (SETB, RSTB, CLK, D, NRESTORE, Q, QN, SAVE );
// module RDFFSRASRX2 (SETB, RSTB, CLK, D, NRESTORE, Q, QN, SAVE );
// module RDFFSRASX1 (SETB, CLK, D, NRESTORE, Q, QN, SAVE);
// module RDFFSRASX2 (SETB, CLK, D, NRESTORE, Q, QN, SAVE);
// module RSDFFNSRARX1 (SI, SE, RSTB, CLK, D, NRESTORE, Q, QN, SAVE);
// module RSDFFNSRARX2 (SI, SE, RSTB, CLK, D, NRESTORE, Q, QN, SAVE);
// module RSDFFNSRASRX1 (SETB, SI, SE,  RSTB, CLK, D, NRESTORE, Q, QN, SAVE);
// module RSDFFNSRASRX2 (SETB, SI, SE,  RSTB, CLK, D, NRESTORE, Q, QN, SAVE);
// module RSDFFNSRASRNX1 (SETB, SI, SE,  RSTB, CLK, D, NRESTORE, QN, SAVE);
// module RSDFFNSRASRNX2 (SETB, SI, SE,  RSTB, CLK, D, NRESTORE, QN, SAVE);
// module RSDFFNSRASRQX1 (SETB, SI, SE,  RSTB, CLK, D, NRESTORE, Q, SAVE);
// module RSDFFNSRASRQX2 (SETB, SI, SE,  RSTB, CLK, D, NRESTORE, Q, SAVE);
// module RSDFFSRARX1 (SI, SE, RSTB, CLK, D, NRESTORE, Q, QN, SAVE);
// module RSDFFSRARX2 (SI, SE, RSTB, CLK, D, NRESTORE, Q, QN, SAVE);
// module RSDFFSRASRX1 (SETB, SI, SE, RSTB, CLK, D, NRESTORE, Q, QN, SAVE);
// module RSDFFSRASRX2 (SETB, SI, SE, RSTB, CLK, D, NRESTORE, Q, QN, SAVE);
// module RDFFNSRARX1 ( RSTB, CLK, D, NRESTORE, Q, QN, SAVE);
// module RDFFNSRARX2 ( RSTB, CLK, D, NRESTORE, Q, QN, SAVE);
// module RDFFNSRASRQX1 (SETB, RSTB, CLK, D, NRESTORE, Q, SAVE);
// module RDFFNSRASRQX2 (SETB, RSTB, CLK, D, NRESTORE, Q, SAVE);
// module RDFFNSRASRNX1 (SETB, RSTB, CLK, D, NRESTORE, QN, SAVE);
// module RDFFNSRASRNX2 (SETB, RSTB, CLK, D, NRESTORE, QN, SAVE);
// module RDFFNSRASRX1 (SETB,  RSTB, CLK, D, NRESTORE, Q, QN, SAVE);
// module RDFFNSRASRX2 (SETB,  RSTB, CLK, D, NRESTORE, Q, QN, SAVE);
// module RDFFNSRX1 ( CLK, D, NRESTORE, Q, QN, SAVE);
// module RDFFNSRX2 ( CLK, D, NRESTORE, Q, QN, SAVE);
// module RDFFSRSSRX1 ( RSTB, SETB, CLK, D, NRESTORE, Q, QN, SAVE);
// module RDFFSRSSRX2 ( RSTB, SETB, CLK, D, NRESTORE, Q, QN, SAVE);
// module RDFFSRX1 ( CLK, D, NRESTORE, Q, QN, SAVE);
// module RDFFSRX2 ( CLK, D, NRESTORE, Q, QN, SAVE);
// module RSDFFNSRASX1 (SETB, SI, SE, CLK, D, NRESTORE, Q, QN, SAVE);
// module RSDFFNSRASX2 (SETB, SI, SE, CLK, D, NRESTORE, Q, QN, SAVE);
// module RSDFFNSRX1 (SI, SE, CLK, D, NRESTORE, Q, QN, SAVE);
// module RSDFFNSRX2 (SI, SE, CLK, D, NRESTORE, Q, QN, SAVE);
// module RSDFFSRASX1 (SETB, SI, SE, CLK, D, NRESTORE, Q, QN, SAVE );
// module RSDFFSRASX2 (SETB, SI, SE, CLK, D, NRESTORE, Q, QN, SAVE );
// module RSDFFSRSSRX1 (SETB, SI, SE, RSTB, CLK, D, NRESTORE, Q, QN, SAVE);
// module RSDFFSRSSRX2 (SETB, SI, SE, RSTB, CLK, D, NRESTORE, Q, QN, SAVE);
// module RSDFFSRX1 ( SI, SE, CLK, D, NRESTORE, Q, QN, SAVE);
// module RSDFFSRX2 ( SI, SE, CLK, D, NRESTORE, Q, QN, SAVE);

