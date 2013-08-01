/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */

#ifndef HAVE_MEM_H
#define HAVE_MEM_H

#define HEADER_SIZE 4096

typedef struct {
	int magic;   // 0
	int endian;  // 4
	int seq;     // 8
	int child_lock;  // 12
	unsigned long size;  // 16
	char handle;  // 24
	char unused[HEADER_SIZE - (4 * sizeof(int)) - (1 * sizeof(long)) - (1 * sizeof(char))];
	int data[];
} mem_t;

mem_t *mem_new(const char *file);
void mem_yield(mem_t *mem);
void mem_acquire(mem_t *mem);

#endif
