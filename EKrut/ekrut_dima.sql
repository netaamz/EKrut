#include <stdio.h>
int main() {
    int pid;
    while (1) {
		pid = fork();
		if (pid < 0) return -1; // Fail
		if (pid == 0) {printf("a\n"); return 1;} // Child prints 'a'
		wait();
		printf("b\n"); // Father prints 'b'
	}
}
