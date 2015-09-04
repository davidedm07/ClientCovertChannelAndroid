#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
//"netdb" per "gethostbyname"
#include <netdb.h>
#include <string.h>
#include <time.h>
#include <ctype.h>



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



int main(int argc,char* argv[]) {

	int sock;
	struct sockaddr_in addr;
	char message[512];
	int i,j;
	int z=0; // contatore per messaggio covert
	char covertMessage[512];
	char SOF[512];
	strcpy(SOF,"Start of Frame"); // messaggio fisso
	int *encoded_covert; // messaggi overt e covert
	int* encoded_overt;  // codificati in binario
	double interval=0.007; // va specificato qui nel metodo e poi il client lo invia al server
	int timing_interval=interval*1000000; 
	//usleep function per aspettare meno di un secondo
	int length_covert=0;

	printf("Inserisci messaggio da inviare\n");
	fgets(message,512,stdin);
	printf("Inserisci messaggio covert da inviare\n");
	fgets(covertMessage,512,stdin);
	printf("Messaggio client = %s\n",message); 
    
    // codifico messaggio covert e overt in binario
	encoded_covert=encode(covertMessage);
	encoded_overt=encode(message);
	length_covert=strlen(covertMessage)*8;

	if ( (sock = socket(PF_INET, SOCK_DGRAM, 0)) < 0) { 
		perror("Socket creation error"); 
		return -1; 
	} 
	/* initialize address */ 
	memset((void *) &addr, 0, sizeof(addr));    /* clear server address */ 
	addr.sin_family = PF_INET;                  /* address type is INET */ 
	addr.sin_port = htons(1745);                  /* server port */ 
	/* build address using inet_pton */ 
	if ( (inet_pton(PF_INET, "127.0.0.1", &addr.sin_addr)) <= 0) { 
		perror("Address creation error"); 
		return -1; 
	} 
	int len=sizeof(addr);

	//invio SOF (start of frame), indico al server che la comunicazione sta
	// per iniziare
	printf("Sending Start of Frame to server\n");
	sendto(sock,&SOF,sizeof(SOF),0,(struct sockaddr *)&addr,sizeof(addr));
	recvfrom(sock, SOF,(int)sizeof(SOF),0,(struct sockaddr*)&addr, &len);
	if(strcmp(SOF,"SOF received")==0) {
		printf("Acknowledgment received for SOF from server\n");
		printf("Sending Data...\n");
		int l=strlen(message);
		printf("Lunghezza messaggio :%d\n",l);
		sendto(sock,&l,sizeof(l),0,(struct sockaddr *)&addr,sizeof(addr));
		//invio del timing interval al server in modo che possa decodificare il messaggio covert
		sendto(sock,&interval,sizeof(interval),0,(struct sockaddr *)&addr,sizeof(addr));
		for (i=0;i<strlen(message)*8;i++) {
			if(z<length_covert) {
				if (encoded_covert[z]==1) {
					usleep(timing_interval/2);
					sendto(sock, &encoded_overt[i],sizeof(encoded_overt[i]), 0, (struct sockaddr *)&addr,sizeof(addr));
					printf("Client: %d",encoded_overt[i]); 
					usleep(timing_interval/2);
				}
				else if (encoded_covert[z]==0) {
					printf("Client: pausa");
					usleep(timing_interval);
					i--;
				}
				printf(" Encoded Bit sent=%d\n",encoded_covert[z]);
				z++;
			}
			else {
				sendto(sock, &encoded_overt[i],sizeof(encoded_overt[i]), 0, (struct sockaddr *)&addr, sizeof(addr));
				printf("Client: %d\n",encoded_overt[i]); 

			}

		}
	}
	printf("Bit Covert inviati: %d\n",z);

	//Chiudo il socket.
	closeSocket(sock);
	return 0;
}
