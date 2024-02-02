// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to delete delivery status from EXTDLS
// Transaction DelDeliveryStat
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * @param: SEQN - Sequence
 * 
*/


 public class DelDeliveryStat extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    
    Integer inCONO
    String inDIVI
  
  // Constructor 
  public DelDeliveryStat(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
     this.mi = mi
     this.database = database 
     this.program = program
     this.logger = logger
  } 
    
  public void main() { 
     // Set Company Number
     inCONO = mi.in.get("CONO")      
     if (inCONO == null || inCONO == 0) {
        inCONO = program.LDAZD.CONO as Integer
     } 

     // Set Division
     inDIVI = mi.in.get("DIVI")
     if (inDIVI == null || inDIVI == "") {
        inDIVI = program.LDAZD.DIVI
     }

     // Delivery Number
     int inDLNO     
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0     
     }

     // Sequence
     int inSEQN      
     if (mi.in.get("SEQN") != null) {
        inSEQN = mi.in.get("SEQN") 
     } else {
        inSEQN = 0     
     }


     // Validate delivery status record
     Optional<DBContainer> EXTDLS = findEXTDLS(inCONO, inDIVI, inDLNO, inSEQN)
     if(!EXTDLS.isPresent()){
        mi.error("Delivery Status doesn't exist")   
        return             
     } else {
        // Delete record 
        deleteEXTDLSRecord(inCONO, inDIVI, inDLNO, inSEQN) 
     } 
     
  }


  //******************************************************************** 
  // Get EXTDLS record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDLS(int CONO, String DIVI, int DLNO, int SEQN){  
     DBAction query = database.table("EXTDLS").index("00").build()
     DBContainer EXTDLS = query.getContainer()
     EXTDLS.set("EXCONO", CONO)
     EXTDLS.set("EXDIVI", DIVI)
     EXTDLS.set("EXDLNO", DLNO)
     EXTDLS.set("EXSEQN", SEQN)
     if(query.read(EXTDLS))  { 
       return Optional.of(EXTDLS)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Delete record from EXTDLS
  //******************************************************************** 
  void deleteEXTDLSRecord(int CONO, String DIVI, int DLNO, int SEQN){ 
     DBAction action = database.table("EXTDLS").index("00").build()
     DBContainer EXTDLS = action.getContainer()
     EXTDLS.set("EXCONO", CONO)
     EXTDLS.set("EXDIVI", DIVI)
     EXTDLS.set("EXDLNO", DLNO)
     EXTDLS.set("EXSEQN", SEQN)

     action.readLock(EXTDLS, deleterCallbackEXTDLS)
  }
    
  Closure<?> deleterCallbackEXTDLS = { LockedResult lockedResult ->  
     lockedResult.delete()
  }
  

 }