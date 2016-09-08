################################################################################
# Automatically-generated file. Do not edit!
################################################################################
CFLAGS=-fstack-protector -fPIE -fPIC -O2 -D_FORTIFY_SOURCE=2 -Wformat -Wformat-security
LDFLAGS=-z noexecstack -z relro -z now -pie

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
./NIARL_TPM_ModuleV2.cpp \
./NIARL_Util_ByteBlob.cpp \
./NIARL_Util_Mask.cpp \
./main.cpp 

OBJS += \
./NIARL_TPM_ModuleV2.o \
./NIARL_Util_ByteBlob.o \
./NIARL_Util_Mask.o \
./main.o 

CPP_DEPS += \
./NIARL_TPM_ModuleV2.d \
./NIARL_Util_ByteBlob.d \
./NIARL_Util_Mask.d \
./main.d 


# Each subdirectory must supply rules for building sources it contributes
%.o: %.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ $(CFLAGS) -O3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o"$@" "$<" $(LDFLAGS)
	@echo 'Finished building: $<'
	@echo ' '


