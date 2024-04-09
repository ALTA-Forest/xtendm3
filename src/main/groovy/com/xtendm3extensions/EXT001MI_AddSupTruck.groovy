// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to add a Supplier Truck to EXTSTR
// Transaction AddSupTruck
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: SUNO - Supplier
 * @param: TRCK - Truck
 * @param: TRNA - Name
 * 
*/


public class AddSupTruck extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  // Constructor 
  public AddSupTruck(MIAPI mi, DatabaseAPI database, UtilityAPI utility, ProgramAPI program, LoggerAPI logger) {
     this.mi = mi
     this.database = database
     this.utility = utility
     this.program = program
     this.logger = logger
  } 
    
  public void main() {       
     // Set Company Number
     int inCONO = program.LDAZD.CONO as Integer

     // Supplier
     String inSUNO
     if (mi.in.get("SUNO") != null && mi.in.get("SUNO") != "") {
        inSUNO = mi.inData.get("SUNO").trim() 
        
        // Validate supplier if entered
        Optional<DBContainer> CIDMAS = findCIDMAS(inCONO, inSUNO)
        if (!CIDMAS.isPresent()) {
           mi.error("Supplier doesn't exist")   
           return             
        }

     } else {
        inSUNO = ""         
     }
      
     // Truck
     String inTRCK
     if (mi.in.get("TRCK") != null && mi.in.get("TRCK") != "") {
        inTRCK = mi.inData.get("TRCK").trim() 
     } else {
        inTRCK = ""        
     }
     
    // Name
     String inTRNA
     if (mi.in.get("TRNA") != null && mi.in.get("TRNA") != "") {
        inTRNA = mi.inData.get("TRNA").trim() 
     } else {
        inTRNA = ""        
     }

     // Validate supplier truck record
     Optional<DBContainer> EXTSTR = findEXTSTR(inCONO, inSUNO, inTRCK)
     if(EXTSTR.isPresent()){
        mi.error("Supplier Truck already exists")   
        return             
     } else {
        // Write record 
        addEXTSTRRecord(inCONO, inSUNO, inTRCK, inTRNA)          
     }  

  }
  
    
  //******************************************************************** 
  // Get EXTSTR record
  //******************************************************************** 
  private Optional<DBContainer> findEXTSTR(int cono, String suno, String trck){  
     DBAction query = database.table("EXTSTR").index("00").build()
     DBContainer EXTSTR = query.getContainer()
     EXTSTR.set("EXCONO", cono)
     EXTSTR.set("EXSUNO", suno)
     EXTSTR.set("EXTRCK", trck)
     if(query.read(EXTSTR))  { 
       return Optional.of(EXTSTR)
     } 
  
     return Optional.empty()
  }


   //******************************************************************** 
   // Check Supplier
   //******************************************************************** 
   private Optional<DBContainer> findCIDMAS(int CONO, String SUNO){  
     DBAction query = database.table("CIDMAS").index("00").build()   
     DBContainer CIDMAS = query.getContainer()
     CIDMAS.set("IDCONO", CONO)
     CIDMAS.set("IDSUNO", SUNO)
    
     if(query.read(CIDMAS))  { 
       return Optional.of(CIDMAS)
     } 
  
     return Optional.empty()
   }

  
  //******************************************************************** 
  // Add EXTSTR record 
  //********************************************************************     
  void addEXTSTRRecord(int cono, String suno, String trck, String trna){     
       DBAction action = database.table("EXTSTR").index("00").build()
       DBContainer EXTSTR = action.createContainer()
       EXTSTR.set("EXCONO", cono)
       EXTSTR.set("EXSUNO", suno)
       EXTSTR.set("EXTRCK", trck)
       EXTSTR.set("EXTRNA", trna) 
       EXTSTR.set("EXCHID", program.getUser())
       EXTSTR.set("EXCHNO", 1)       
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
       EXTSTR.set("EXRGDT", regdate) 
       EXTSTR.set("EXLMDT", regdate) 
       EXTSTR.set("EXRGTM", regtime)
       action.insert(EXTSTR)         
 } 

     
} 

