/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */

#include<stdio.h>
//#include<stdlib.h>

#include "mem.h"

int main(int argc, const char **argv) {

	mem_t *mem[10];
	int i;

	for (i = 1; i < argc; i++) {
		mem[i-1] = mem_new(argv[i]);
		mem_yield(mem[i-1]);
	}

	mem_acquire(mem[0]);
	int a = mem[0]->data[10];
	int b = mem[0]->data[11];
	mem[0]->data[12] = a + b;
	mem_yield(mem[0]);

	while(getc(stdin) != EOF);
	return 0;
}
