using AutoMapper;

public class MappingProfile : Profile
{
    public MappingProfile()
    {
        // CreateMap<ReqisterRequest, User>()
        //     .ForMember(dest => dest.Email, opt => opt.MapFrom(src => src.Email))
        //     .ForMember(dest => dest.FirstName, opt => opt.MapFrom(src => src.FirstName))
        //     .ForMember(dest => dest.LastName, opt => opt.MapFrom(src => src.LastName))
        //     .ForMember(dest => dest.PhoneNumber, opt => opt.MapFrom(src => src.PhoneNumber));
    }
}
