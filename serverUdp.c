#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
//"in" per "sockaddr_in"
#include <netinet/in.h>
//"fcntl" per la funzione "fcntl"
#include <fcntl.h>
#include <string.h>
#include <time.h>


void closeSocket(int sock) {
  close(sock);
  return;
}

char* decode(char* x) {
  int len=strlen(x)/8; // lunghezza messaggio codificato
  int i,j;
  int pos=0;
  char temp[9];
  char c;
  char* endP=NULL;
  char* decoded=(char*)calloc(len+1,sizeof(char));
  for(i=0; i<len; i++) {
    for (j=0;j<8;j++) {
      temp[j]=x[j+pos];
    }
    c=strtoul(temp,&endP,2);
    decoded[i]=c;
    pos=pos+8;
  }
  decoded[len]='\0';
  return decoded;

}

typedef struct elem {
  int value;
  struct elem* next;
}list;

typedef list* plist;

// fa abbastanza schifo, modifica se possibile
void insertCovert(plist* l, int zeros, int* length) {
  int i;
  plist x=*l;
  if (x== NULL) {
    x=(plist)malloc(sizeof(list));
    x->value=0;
    x->next=NULL;  
    zeros--; 
    *l=x; 
  }
  while(x->next!=NULL)
    x=x->next;

    for (i=0;i<zeros;i++) {
      x->next=(plist)malloc(sizeof(list));
      x=x->next;
      x->value=0;
      x->next=NULL;
    }

    x->next=(plist)malloc(sizeof(list));
    x=x->next;
    x->value=1;
    x->next=NULL;

    *length=*length+zeros+1;

}

char* list_to_array(plist l,int length) {
  char* array=(char*)calloc(length+1,sizeof(char));
  int i;
  for(i=0;i<length;i++) {
    if(l->value==1)
      array[i]='1';
    else
      array[i]='0';
    l=l->next;
  }
  array[length]='\0';
  return array;
}

int main() {
  int sock;
  int bitOvert;
  char SOF[512];
  struct sockaddr_in addr; 
  int n,len;
  int length; // lunghezza messaggio overt
  int i=0;
  int j=0;
  char *temp;
  char* overt=NULL;
  char *endP=NULL; // utilizzato solo per strtoul
  struct timespec now,after;
  char* c_time_string;
  double difference=0;
  double timing_interval;

  plist covert=NULL; // lista che contiene tutti i bit covert
  int length_covert=0; // lunghezza lista covert
  char* covert_message;

  if ( (sock = socket(PF_INET, SOCK_DGRAM, 0)) < 0) { 
    perror("Socket creation error"); 
    exit(-1); 
  } 

  /* initialize address */ 
  memset((void *)&addr, 0, sizeof(addr));     /* clear server address */ 
  addr.sin_family = PF_INET;                  /* address type is INET */ 
  addr.sin_port = htons(1745);                   
  addr.sin_addr.s_addr = htonl(INADDR_ANY);   /* connect from anywhere */ 
  len=sizeof(addr);
  /* bind socket */ 
  if (bind(sock, (struct sockaddr *)&addr,sizeof(addr)) < 0) { 
    perror("bind error"); 
    exit(-1); 
  } 

  printf("Server: Attendo connessioni...\n");
  //ricezione SOF
  recvfrom(sock, SOF,sizeof(SOF),0,(struct sockaddr*)&addr, &len);

  if(strcmp(SOF,"Start of Frame")==0){
    printf("%s\n",SOF);
    strcpy(SOF,"SOF received");
    sendto(sock,&SOF,sizeof(SOF),0,(struct sockaddr *)&addr,len);
    recvfrom(sock, &length,sizeof(int),0,(struct sockaddr*)&addr, &len);
    printf("Lunghezza messaggio: %d\n",length);
    length=length*8;
    temp=(char*)calloc(length,sizeof(char));
    recvfrom(sock, &timing_interval,sizeof(timing_interval),0,(struct sockaddr*)&addr, &len);
    printf("Timing interval= %f\n",timing_interval);

    printf("Server: connessione accettata\n");
    while(i<length) {
      clock_gettime(CLOCK_MONOTONIC, &now);
      n=recvfrom(sock,&bitOvert,sizeof(int),0,(struct sockaddr*) &addr, &len);
      clock_gettime(CLOCK_MONOTONIC, &after);     
      difference=((double)after.tv_sec + 1.0e-9*after.tv_nsec) -((double)now.tv_sec + 1.0e-9*now.tv_nsec);
      printf("Differenza tempo : %5f  ", difference);
      difference=difference-timing_interval/2; // timing interval/2
      int zeros=(int)(difference/timing_interval); // timing interval deve essere noto al server RISOLVI
      if(difference>0) // sto ancora inviando in covert mode
        insertCovert(&covert,zeros,&length_covert);
      if (n<0) {
        perror("Error recvfrom\n");
        exit(1);
      }
      printf("Server: %d\n",bitOvert);
      if (bitOvert==1)
        temp[i]='1';
      else if (bitOvert==0)
        temp[i]='0';

      i++;
      j++;

      if(j>=8 && j%8==0)
        printf("\n"); // stampa più pulita
    }

    temp[length]='\0';
    overt=decode(temp);
    covert_message=list_to_array(covert,length_covert);
    covert_message=decode(covert_message);
    printf("Messaggio decodificato: %s\n",overt);
    printf("Messaggio covert: %s\n",covert_message);

  }
  printf("Server Terminato\n");
  closeSocket(sock);
}