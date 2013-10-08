#include <stdio.h>
#include <string.h>
#include <trousers/tss.h>
#include "remote.h"

UNICODE* ascii_to_unicode(char *src) {
        int i, max=strlen(src);
        UNICODE *dst = (UNICODE *)malloc( (max+1) * sizeof(UNICODE) );
        for(i=0; i<max; i++) {
                BYTE high, low;
                high = 0; // read_byte(ptr, &high);
                low = (BYTE)src[i]; //read_byte(ptr, &low);
                dst[i] = (high<<8) | low;
                if( DEBUG_UNICODE ) printf("read uint16: %lu = %u %u\n", (unsigned long int)dst[i], (unsigned int)high, (unsigned int)low);
        }
        return (UNICODE *)dst;
}

