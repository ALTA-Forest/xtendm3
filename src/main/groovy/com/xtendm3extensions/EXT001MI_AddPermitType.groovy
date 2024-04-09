// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to add a new permit to EXTPTT
// Transaction AddPermitType
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: PTPC - Permit Type
 * @param: PTNA - Name
 * @param: PTSW - Slash Withheld
 * @param: PTDE - Description
 * @param: PTDT - Expiration Date
 * 
*/



public class AddPermitType extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database
    private final ProgramAPI program
    private final UtilityAPI utility
    private final LoggerAPI logger
    
    // Constructor 
    public AddPermitType(MIAPI mi, DatabaseAPI database, UtilityAPI utility, ProgramAPI program, LoggerAPI logger) {
       this.mi = mi
       this.database = database
       this.utility = utility
       this.program = program
       this.logger = logger
    } 
      
    public void main() {       
       // Set Company Number
       int inCONO = program.LDAZD.CONO as Integer
 
       // Permit Type
       String inPTPC
       if (mi.in.get("PTPC") != null && mi.in.get("PTPC") != "") {
          inPTPC = mi.inData.get("PTPC").trim() 
       } else {
          inPTPC = ""         
       }
        
       // Name
       String inPTNA
       if (mi.in.get("PTNA") != null && mi.in.get("PTNA") != "") {
          inPTNA = mi.inData.get("PTNA").trim() 
       } else {
          inPTNA = ""        
       }
       
       // Slash Withheld
       int inPTSW
       if (mi.in.get("PTSW") != null) {
          inPTSW = mi.in.get("PTSW") 
       } else {
          inPTSW = 0       
       }
       
       // Description
       String inPTDE
       if (mi.in.get("PTDE") != null && mi.in.get("PTDE") != "") {
          inPTDE = mi.inData.get("PTDE").trim() 
       } else {
          inPTDE = ""        
       }
  
       // Expiration Date
       int inPTDT
       if (mi.in.get("PTDT") != null) {
          inPTDT = mi.in.get("PTDT") 
          
          //Validate date format
          boolean validPTDT = utility.call("DateUtil", "isDateValid", String.valueOf(inPTDT), "yyyyMMdd")  
          if (!validPTDT) {
              mi.error("Expiration Date is not valid")   
              return  
          } 

       } else {
          inPTDT = 0        
       }
  
       
       // Validate permit record
       Optional<DBContainer> EXTPTT = findEXTPTT(inCONO, inPTPC)
       if(EXTPTT.isPresent()){
          mi.error("Permit already exists")   
          return             
       } else {
          // Write record 
          addEXTPTTRecord(inCONO, inPTPC, inPTNA, inPTSW, inPTDE, inPTDT)          
       }  
  
    }
    

  //******************************************************************** 
    // Get EXTPTT record
    //******************************************************************** 
    private Optional<DBContainer> findEXTPTT(int CONO, String PTPC){  
       DBAction query = database.table("EXTPTT").index("00").build()
       DBContainer EXTPTT = query.getContainer()
       EXTPTT.set("EXCONO", CONO)
       EXTPTT.set("EXPTPC", PTPC)
       if(query.read(EXTPTT))  { 
         return Optional.of(EXTPTT)
       } 
    
       return Optional.empty()
    }
    
    //******************************************************************** 
    // Add EXTPTT record 
    //********************************************************************     
    void addEXTPTTRecord(int CONO, String PTPC, String PTNA, int PTSW, String PTDE, int PTDT){     
         DBAction action = database.table("EXTPTT").index("00").build()
         DBContainer EXTPTT = action.createContainer()
         EXTPTT.set("EXCONO", CONO)
         EXTPTT.set("EXPTPC", PTPC)
         EXTPTT.set("EXPTNA", PTNA)
         EXTPTT.set("EXPTSW", PTSW)
         EXTPTT.set("EXPTDE", PTDE)
         EXTPTT.set("EXPTDT", PTDT)     
         EXTPTT.set("EXCHID", program.getUser())
         EXTPTT.set("EXCHNO", 1)  
         int regdate = utility.call("DateUtil", "currentDateY8AsInt")
         int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
         EXTPTT.set("EXRGDT", regdate) 
         EXTPTT.set("EXLMDT", regdate) 
         EXTPTT.set("EXRGTM", regtime)
         action.insert(EXTPTT)         
   } 

     
} 

