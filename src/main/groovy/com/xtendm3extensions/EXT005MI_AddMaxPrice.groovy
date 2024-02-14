// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2024-02-09
// @version   1.0 
//
// Description 
// This API is to add a max price record to EXTMAX
// Transaction AddMaxPrice
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: CTYP - Revision ID
 * @param: RTPC - Rate Type
 * @param: AMNT - Amount
 * 
*/



public class AddMaxPrice extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI

  
  // Constructor 
  public AddMaxPrice(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
     this.mi = mi
     this.database = database
     this.program = program
     this.logger = logger
     this.utility = utility
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

     // Contract Type
     int inCTYP
     if (mi.in.get("CTYP") != null) {
        inCTYP = mi.in.get("CTYP") 
     } else {
        inCTYP = 0       
     }
       
     // Rate Type
     String inRTPC
     if (mi.in.get("RTPC") != null) {
        inRTPC = mi.inData.get("RTPC").trim() 
     } else {
        inRTPC = ""        
     }
     
     // Amount
     double inAMNT
     if (mi.in.get("AMNT") != null) {
        inAMNT = mi.in.get("AMNT") 
     } else {
        inAMNT = 0d       
     }
     
     
     // Validate max price record
     Optional<DBContainer> EXTMAX = findEXTMAX(inCONO, inDIVI, inCTYP, inRTPC)
     if(EXTMAX.isPresent()){
        mi.error("Max Price already exists")   
        return             
     } else {
        // Write record 
        addEXTMAXRecord(inCONO, inDIVI, inCTYP, inRTPC, inAMNT)          
     }  

  }
  
    
  //******************************************************************** 
  // Get EXTMAX record
  //******************************************************************** 
  private Optional<DBContainer> findEXTMAX(int CONO, String DIVI, int CTYP, String RTPC){  
     DBAction query = database.table("EXTMAX").index("00").build()
     DBContainer EXTMAX = query.getContainer()
     EXTMAX.set("EXCONO", CONO)
     EXTMAX.set("EXDIVI", DIVI)
     EXTMAX.set("EXCTYP", CTYP)
     EXTMAX.set("EXRTPC", RTPC)
     if(query.read(EXTMAX))  { 
       return Optional.of(EXTMAX)
     } 
  
     return Optional.empty()
  }
  
  
  //******************************************************************** 
  // Add EXTMAX record 
  //********************************************************************     
  void addEXTMAXRecord(int CONO, String DIVI, int CTYP, String RTPC, double AMNT){ 
       DBAction action = database.table("EXTMAX").index("00").build()
       DBContainer EXTMAX = action.createContainer()
       EXTMAX.set("EXCONO", CONO)
       EXTMAX.set("EXDIVI", DIVI)
       EXTMAX.set("EXCTYP", CTYP)
       EXTMAX.set("EXRTPC", RTPC)
       EXTMAX.set("EXAMNT", AMNT)
       EXTMAX.set("EXCHID", program.getUser())
       EXTMAX.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")    
       EXTMAX.set("EXRGDT", regdate) 
       EXTMAX.set("EXLMDT", regdate) 
       EXTMAX.set("EXRGTM", regtime)
       action.insert(EXTMAX)         
 } 
     
} 

