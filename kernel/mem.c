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
#include<stdlib.h>
#include<sys/stat.h>
#include<sys/mman.h>
#include<fcntl.h>
#include<unistd.h>

#include"mem.h"

#define FILE_MAGIC 0x23232323
#define ENDIAN_MAGIC 0x76543210

#define MSG_YIELD 100

mem_t *mem_new(const char *file) {
	mem_t *mem;
	struct stat stats;
	int fd = open(file, O_RDWR);
	if (fd < 0) {
		fprintf(stderr, "mem_new failed: open(%s) returned %i.\n", file, fd);
		exit(1);
	}
	fstat(fd, &stats);
	mem = (mem_t*) mmap(NULL, stats.st_size, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
	close(fd);
	if (mem == (mem_t*) -1) {
		fprintf(stderr, "mem_new failed: mmap(%s) returned %li.\n", file, (long) mem->data);
		exit(1);
	}
	if ((unsigned long) mem->data - (unsigned long) mem != HEADER_SIZE) {
		fprintf(stderr, "mem_new failed: header size mismatch (%li != %i), fix your mem_t and re-compile.\n",
				(unsigned long) mem->data - (unsigned long) mem, HEADER_SIZE);
		exit(1);
	}
	if (mem->magic != FILE_MAGIC) {
		fprintf(stderr, "mem_new failed: wrong file magic in %s (%x).\n", file, mem->magic);
		exit(1);
	}
	if (mem->endian != ENDIAN_MAGIC) {
		fprintf(stderr, "mem_new failed: endian mismatch for %s (%x).\n", file, mem->endian);
		exit(1);
	}
	mem->size = (unsigned long) stats.st_size;
	mem->child_lock = 1;
	//fprintf(stderr, "mem_new memory %i with %li bytes from %s.\n", (int) mem->handle, mem->size, file);
	return mem;
}

void mem_yield(mem_t *mem) {
	if (!mem->child_lock) {
		fprintf(stderr, "mem_yield failed: memory %i is already yielded.\n", (int) mem->handle);
		exit(1);
	}
	mem->child_lock = 0;
	mem->seq++;
	putc(MSG_YIELD, stdout);
	putc(mem->handle, stdout);
	putc((char) mem->seq, stdout);
	fflush(stdout);
}

void mem_acquire(mem_t *mem) {
	if (mem->child_lock == 1) {
		fprintf(stderr, "mem_acquire failed: memory %i is already acquired.\n", (int) mem->handle);
		exit(1);
	}
	char seq;
	if (getc(stdin) != MSG_YIELD) {
		fprintf(stderr, "mem_acquire failed: no yield from parent.");
		exit(1);
	}

	if (getc(stdin) != mem->handle) {
		fprintf(stderr, "mem_acquire failed: invalid handle from parent.");
		exit(1);
	}
	seq = getc(stdin);
	int polling = 0;
	while (seq != (char) mem->seq) {
		usleep(1000);
		if ((++polling % 1000) != 0)
			fprintf(stderr, "mem_acquire: busy waiting on memory %i ...\n", (int) mem->handle);
	}
	mem->child_lock = 1;
}

