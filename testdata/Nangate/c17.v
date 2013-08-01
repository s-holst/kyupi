
module c17 ( X1gatX, X2gatX, X3gatX, X6gatX, X7gatX, X22gatX, X23gatX );
  input X1gatX, X2gatX, X3gatX, X6gatX, X7gatX;
  output X22gatX, X23gatX;
  wire   n4, n5, n6;

  NAND2_X1 U6 ( .A1(X3gatX), .A2(X6gatX), .ZN(n5) );
  AOI22_X1 U7 ( .A1(X3gatX), .A2(X1gatX), .B1(X2gatX), .B2(n5), .ZN(n4) );
  INV_X1 U8 ( .A(n4), .ZN(X22gatX) );
  OAI21_X1 U9 ( .B1(X2gatX), .B2(X7gatX), .A(n5), .ZN(n6) );
  INV_X1 U10 ( .A(n6), .ZN(X23gatX) );
endmodule

