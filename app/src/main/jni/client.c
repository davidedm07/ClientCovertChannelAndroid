//
// Created by davide on 17/08/15.
//

#include "client.h"
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>

//"netdb" per "gethostbyname"



void closeSocket(int socket) {
    close(socket);
    return;
}

// trasforma un carattere in codice binario
int* encodeChar (char c) {
    int *output=(int*)calloc(8,sizeof(int));
    int i;
    for (i=0;i<8;i++) {
        output[7-i]=((c>>i)& 1);
    }
    return output;

}

// codifica un messaggio in binario e lo pone in un array
int* encode(char* message) {
    int length=(int)strlen(message);
    int* encoded = (int*)calloc(8*length,sizeof(int));
    int i,j;
    int pos=0;
    int* single_char=(int*)calloc(8,sizeof(int));
    for (i=0;i<length;i++) {
        single_char=encodeChar(message[i]);
        for(j=0;j<8;j++)
            encoded[j+pos]=single_char[j];
        pos= pos+8; // per ricordare la posizione corrente nell'array finale
    }
    return encoded;

}
int createAndSendSocket(char* address, int port, char* overt, char* covert,int timing) {
    int sock;
    struct sockaddr_in addr;
    double interval=timing/1000.0;
    int timing_interval= (int) (interval*1000000);
    int i,j; // counters
    int cont=0; //number of covert bits sent
    char sync_message[6];
    char sync[5];
    strcpy(sync,"SYNC");
    if ( (sock = socket(PF_INET, SOCK_DGRAM, 0)) < 0) {
        perror("Socket creation error");
        return -1;
    }
    /* initialize address */
    memset((void *) &addr, 0, sizeof(addr));    /* clear server address */
    addr.sin_family = PF_INET;                  /* address type is INET */
    addr.sin_port = htons(port);
    /* build address using inet_pton */
    if ( (inet_pton(PF_INET, address, &addr.sin_addr)) <= 0) {
        perror("Address creation error");
        return -1;
    }

    int* encodedOvert=encode(overt);
    int* encodedCovert=encode(covert);
    int length_covert= (int) (strlen(covert)*8);
    j=0;
    int l= (int) (strlen(overt)*8);

    sendto(sock,&l,sizeof(l),0,(struct sockaddr *)&addr,sizeof(addr));
    sendto(sock,&interval,sizeof(interval),0,(struct sockaddr *)&addr,sizeof(addr));

    char c; // carattere che indica il numero di timing slot passati
    int len= (int) strlen(sync);
    for (i=0;i<strlen(overt)*8;i++) {
        if(j<length_covert) {
            if (encodedCovert[j]==1) {
                usleep((useconds_t) (timing_interval/2));
                sendto(sock, &encodedOvert[i],sizeof(int), 0, (struct sockaddr *)&addr,sizeof(addr));
                usleep((useconds_t) (timing_interval/2));
                cont++;
                // ogni volta che invio un pacchetto comunico al server quanti bit covert
                // ho inviato prima dell'1, sincronizzo i tempi
                c=cont+'0';
                strcpy(sync_message,sync);
                sync_message[len]=c;
                sync_message[len+1]= '\0';
                sendto(sock,&sync_message,sizeof(sync_message),0,(struct sockaddr*)&addr,sizeof(addr));
                cont=0; // riparte il contatore di cont

            }
            else if (encodedCovert[j]==0) {
                cont++;
                usleep((useconds_t) timing_interval);
                i--;

            }
            j++;
        }
        else if (cont!=0 ) {
            /* se ho un messaggio covert che termina solo con  0 ho bisogno comunque di inviare almeno
             * un pacchetto di sync */
            sendto(sock, &encodedOvert[i], sizeof(int), 0, (struct sockaddr *) &addr, sizeof(addr));
            len= (int) strlen(sync);
            strcpy(sync_message,sync);
            c=cont+'0';
            sync_message[len]=c;
            sync_message[len+1]= '\0';
            sendto(sock,&sync_message,sizeof(sync_message),0,(struct sockaddr*)&addr,sizeof(addr));
            cont=0;
        }
        else
            sendto(sock, &encodedOvert[i], sizeof(int), 0, (struct sockaddr *) &addr, sizeof(addr));

    }
    closeSocket(sock);
    return 1;

}

jint Java_com_example_client_SendActivity_sendFromJNI( JNIEnv* env, jobject this ,jstring address, jint port,jstring overt,jstring covert,jint interval) {
#if defined(__arm__)
    #if defined(__ARM_ARCH_7A__)
      #if defined(__ARM_NEON__)
        #if defined(__ARM_PCS_VFP)
          #define ABI "armeabi-v7a/NEON (hard-float)"
        #else
          #define ABI "armeabi-v7a/NEON"
        #endif
      #else
        #if defined(__ARM_PCS_VFP)
          #define ABI "armeabi-v7a (hard-float)"
        #else
          #define ABI "armeabi-v7a"
        #endif
      #endif
    #else
     #define ABI "armeabi"
    #endif
#elif defined(__i386__)
    #define ABI "x86"
#elif defined(__x86_64__)
    #define ABI "x86_64"
#elif defined(__mips64)  /* mips64el-* toolchain defines __mips__ too */
    #define ABI "mips64"
#elif defined(__mips__)
    #define ABI "mips"
#elif defined(__aarch64__)
#define ABI "arm64-v8a"
#else
    #define ABI "unknown"
#endif

    const char *nativeAddress = (*env)->GetStringUTFChars(env, address, 0);
    const char *nativeOvert = (*env)->GetStringUTFChars(env, overt, 0);
    const char *nativeCovert = (*env)->GetStringUTFChars(env, covert, 0);
    int nativePort=(int)port;
    int nativeInterval=(int)interval;
    jint result=createAndSendSocket(nativeAddress,nativePort,nativeOvert,nativeCovert,nativeInterval);
    return result;
}

