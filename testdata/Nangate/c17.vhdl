
library IEEE;

use IEEE.std_logic_1164.all;

package CONV_PACK_c17 is

-- define attributes
attribute ENUM_ENCODING : STRING;

end CONV_PACK_c17;

library IEEE;

use IEEE.std_logic_1164.all;

use work.CONV_PACK_c17.all;

entity c17 is

   port( X1gatX, X2gatX, X3gatX, X6gatX, X7gatX : in std_logic;  X22gatX, 
         X23gatX : out std_logic);

end c17;

architecture SYN_NL of c17 is

   component INV_X1
      port( A : in std_logic;  ZN : out std_logic);
   end component;
   
   component OAI21_X1
      port( B1, B2, A : in std_logic;  ZN : out std_logic);
   end component;
   
   component AOI22_X1
      port( A1, A2, B1, B2 : in std_logic;  ZN : out std_logic);
   end component;
   
   component NAND2_X1
      port( A1, A2 : in std_logic;  ZN : out std_logic);
   end component;
   
   signal n4, n5, n6 : std_logic;

begin
   
   U6 : NAND2_X1 port map( A1 => X3gatX, A2 => X6gatX, ZN => n5);
   U7 : AOI22_X1 port map( A1 => X3gatX, A2 => X1gatX, B1 => X2gatX, B2 => n5, 
                           ZN => n4);
   U8 : INV_X1 port map( A => n4, ZN => X22gatX);
   U9 : OAI21_X1 port map( B1 => X2gatX, B2 => X7gatX, A => n5, ZN => n6);
   U10 : INV_X1 port map( A => n6, ZN => X23gatX);

end SYN_NL;
